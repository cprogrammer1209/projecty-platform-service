package com.projecty.platform.modules.portal.internal.service.impl;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.projecty.platform.common.constants.AppConstants;
import com.projecty.platform.common.exception.AppException;
import com.projecty.platform.common.service.RequestPortalContext;
import com.projecty.platform.modules.audit.api.AuditWriteService;
import com.projecty.platform.modules.audit.api.dto.AuditEventCreateDTO;
import com.projecty.platform.modules.outbox.api.OutboxWriteService;
import com.projecty.platform.modules.outbox.api.dto.OutboxEventCreateDTO;
import com.projecty.platform.modules.portal.api.dto.PortalSummaryResponseDTO;
import com.projecty.platform.modules.portal.api.event.PortalCreatedEvent;
import com.projecty.platform.modules.portal.internal.constants.PortalConstants;
import com.projecty.platform.modules.portal.internal.dto.CurrentPortalResponseDTO;
import com.projecty.platform.modules.portal.internal.dto.PortalCreateDTO;
import com.projecty.platform.modules.portal.internal.dto.PortalResponseDTO;
import com.projecty.platform.modules.portal.internal.dto.PortalSettingsUpdateDTO;
import com.projecty.platform.modules.portal.internal.dto.SlugAvailabilityResponseDTO;
import com.projecty.platform.modules.portal.internal.entity.Portal;
import com.projecty.platform.modules.portal.internal.helper.PortalCache;
import com.projecty.platform.modules.portal.internal.helper.SlugRulesHelper;
import com.projecty.platform.modules.portal.internal.mapper.PortalMapper;
import com.projecty.platform.modules.portal.internal.repository.PortalRepository;
import com.projecty.platform.modules.portal.internal.service.PortalService;
import com.projecty.platform.modules.shared.constants.AuditEventConstants;
import com.projecty.platform.modules.shared.constants.OutboxEventConstants;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Composite service: owns portals, their slugs and settings. Membership belongs to the member module, which
 * hears about new portals through {@code PortalCreatedEvent}.
 */
@Service
public class PortalServiceImpl implements PortalService {

	private final PortalRepository portalRepository;

	private final PortalMapper portalMapper;

	private final RequestPortalContext portalContext;

	private final AuditWriteService auditWriteService;

	private final OutboxWriteService outboxWriteService;

	private final ApplicationEventPublisher events;

	private final PortalCache portalCache;

	private final Clock clock;

	public PortalServiceImpl(PortalRepository portalRepository, PortalMapper portalMapper,
			RequestPortalContext portalContext, AuditWriteService auditWriteService,
			OutboxWriteService outboxWriteService, ApplicationEventPublisher events, PortalCache portalCache,
			Clock clock) {
		this.portalRepository = portalRepository;
		this.portalMapper = portalMapper;
		this.portalContext = portalContext;
		this.auditWriteService = auditWriteService;
		this.outboxWriteService = outboxWriteService;
		this.events = events;
		this.portalCache = portalCache;
		this.clock = clock;
	}

	@Override
	@Transactional(readOnly = true)
	public List<PortalSummaryResponseDTO> findAllPortals() {
		return this.portalRepository.findAllByOrderByCreatedAtDesc().stream().map(this.portalMapper::toSummary).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<PortalSummaryResponseDTO> findPortal(UUID portalId) {
		return this.portalCache.findPortal(portalId,
				() -> this.portalRepository.findById(portalId).map(this.portalMapper::toSummary));
	}

	@Override
	@Transactional(readOnly = true)
	public List<PortalSummaryResponseDTO> findByIds(Collection<UUID> portalIds) {
		return this.portalCache.findByIds(portalIds,
				(ids) -> this.portalRepository.findAllById(ids).stream().map(this.portalMapper::toSummary).toList());
	}

	@Override
	@Transactional
	public PortalResponseDTO create(UUID userId, String userName, PortalCreateDTO request) {
		String slug = request.slug();
		Optional<String> slugProblem = SlugRulesHelper.problem(slug);
		if (slugProblem.isPresent()) {
			throw new AppException(HttpStatus.BAD_REQUEST, PortalConstants.ERROR_SLUG_INVALID, slugProblem.get());
		}
		if (this.portalRepository.existsBySlug(slug)) {
			throw slugTaken(slug);
		}
		Instant now = this.clock.instant();
		Portal portal;
		try {
			portal = this.portalRepository
				.saveAndFlush(new Portal(request.name().strip(), slug, userId, initialTimeZone(request.timeZone()), now));
		}
		catch (DataIntegrityViolationException ex) {
			// Another request took the slug between the check and the insert.
			throw slugTaken(slug);
		}
		this.events.publishEvent(new PortalCreatedEvent(portal.getId(), userId));
		this.auditWriteService.record(new AuditEventCreateDTO(AuditEventConstants.CATEGORY_ADMIN,
				AuditEventConstants.ACTION_PORTAL_CREATED, AuditEventConstants.ACTOR_USER, userId, userName,
				portal.getId(), userId, userName, "Created portal " + portal.getName()));
		this.outboxWriteService.publish(new OutboxEventCreateDTO(portal.getId(),
				OutboxEventConstants.AGGREGATE_PORTAL, portal.getId(), OutboxEventConstants.EVENT_PORTAL_CREATED,
				userId, userName, Map.of("portalId", portal.getId(), "slug", portal.getSlug(), "ownerUserId", userId)));
		return this.portalMapper.toResponse(portal);
	}

	@Override
	@Transactional(readOnly = true)
	public CurrentPortalResponseDTO getCurrent() {
		return this.portalCache.current(this.portalContext.requirePortalId(),
				() -> this.portalMapper.toCurrent(currentPortal()));
	}

	@Override
	@Transactional
	public CurrentPortalResponseDTO updateSettings(UUID userId, String userName, PortalSettingsUpdateDTO request) {
		validateSettings(request);
		Portal portal = currentPortal();
		String changes = describeChanges(portal, request);
		portal.updateSettings(request.timeZone(), request.dateFormat(), request.currency(), request.weekStart(),
				this.clock.instant());
		if (!changes.isEmpty()) {
			this.auditWriteService.record(new AuditEventCreateDTO(AuditEventConstants.CATEGORY_ADMIN,
					AuditEventConstants.ACTION_PORTAL_SETTINGS_CHANGED, AuditEventConstants.ACTOR_USER, userId,
					userName, portal.getId(), null, null, changes));
			this.outboxWriteService.publish(new OutboxEventCreateDTO(portal.getId(),
					OutboxEventConstants.AGGREGATE_PORTAL, portal.getId(),
					OutboxEventConstants.EVENT_PORTAL_SETTINGS_CHANGED, userId, userName,
					Map.of("portalId", portal.getId(), "changes", changes)));
			this.portalCache.changedAfterCommit(portal.getId());
		}
		return this.portalMapper.toCurrent(portal);
	}

	@Override
	@Transactional(readOnly = true)
	public SlugAvailabilityResponseDTO checkSlug(String slug) {
		Optional<String> slugProblem = SlugRulesHelper.problem(slug);
		if (slugProblem.isPresent()) {
			return new SlugAvailabilityResponseDTO(false, slugProblem.get(), suggestions(slug));
		}
		if (this.portalRepository.existsBySlug(slug)) {
			return new SlugAvailabilityResponseDTO(false, PortalConstants.MESSAGE_SLUG_TAKEN, suggestions(slug));
		}
		return new SlugAvailabilityResponseDTO(true, null, List.of());
	}

	/**
	 * The request's portal. PortalContextFilter already checked that the caller is an active member.
	 */
	private Portal currentPortal() {
		return this.portalRepository.findById(this.portalContext.requirePortalId())
			.orElseThrow(PortalServiceImpl::portalNotFound);
	}

	private static String initialTimeZone(String requested) {
		return (requested != null && isValidZone(requested)) ? requested : PortalConstants.DEFAULT_TIME_ZONE;
	}

	private static void validateSettings(PortalSettingsUpdateDTO request) {
		Map<String, Object> errors = new LinkedHashMap<>();
		if (!isValidZone(request.timeZone())) {
			errors.put(PortalConstants.FIELD_TIME_ZONE, PortalConstants.MESSAGE_TIME_ZONE_INVALID);
		}
		if (!PortalConstants.ALLOWED_DATE_FORMATS.contains(request.dateFormat())) {
			errors.put(PortalConstants.FIELD_DATE_FORMAT, PortalConstants.MESSAGE_DATE_FORMAT_INVALID);
		}
		if (!isValidCurrency(request.currency())) {
			errors.put(PortalConstants.FIELD_CURRENCY, PortalConstants.MESSAGE_CURRENCY_INVALID);
		}
		if (!errors.isEmpty()) {
			throw new AppException(HttpStatus.BAD_REQUEST, AppConstants.ERROR_VALIDATION_FAILED,
					AppConstants.MESSAGE_VALIDATION_FAILED, errors);
		}
	}

	private static boolean isValidZone(String zone) {
		try {
			ZoneId.of(zone);
			return true;
		}
		catch (DateTimeException ex) {
			return false;
		}
	}

	private static boolean isValidCurrency(String code) {
		try {
			Currency.getInstance(code);
			return true;
		}
		catch (IllegalArgumentException ex) {
			return false;
		}
	}

	/**
	 * "Currency USD → EUR; Time zone UTC → Asia/Kolkata" for the audit log; empty if nothing changed.
	 */
	private static String describeChanges(Portal portal, PortalSettingsUpdateDTO request) {
		List<String> changes = new ArrayList<>();
		addChange(changes, PortalConstants.LABEL_TIME_ZONE, portal.getTimeZone(), request.timeZone());
		addChange(changes, PortalConstants.LABEL_DATE_FORMAT, portal.getDateFormat(), request.dateFormat());
		addChange(changes, PortalConstants.LABEL_CURRENCY, portal.getCurrency(), request.currency());
		addChange(changes, PortalConstants.LABEL_WEEK_START, portal.getWeekStart().name(), request.weekStart().name());
		return String.join("; ", changes);
	}

	private static void addChange(List<String> changes, String label, String before, String after) {
		if (!before.equals(after)) {
			changes.add(label + " " + before + " → " + after);
		}
	}

	private static AppException portalNotFound() {
		return new AppException(HttpStatus.NOT_FOUND, PortalConstants.ERROR_PORTAL_NOT_FOUND,
				PortalConstants.MESSAGE_PORTAL_NOT_FOUND);
	}

	private AppException slugTaken(String slug) {
		return new AppException(HttpStatus.CONFLICT, PortalConstants.ERROR_SLUG_TAKEN,
				PortalConstants.MESSAGE_SLUG_TAKEN, Map.of(PortalConstants.DETAIL_SUGGESTIONS, suggestions(slug)));
	}

	private List<String> suggestions(String slug) {
		Set<String> candidates = SlugRulesHelper.candidates(slug);
		candidates.removeAll(this.portalRepository.findExistingSlugs(candidates));
		return candidates.stream().limit(PortalConstants.MAX_SLUG_SUGGESTIONS).toList();
	}

}
