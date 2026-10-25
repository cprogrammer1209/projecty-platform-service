package com.projecty.platform.modules.portal.internal.service.impl;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
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
import com.projecty.platform.modules.portal.api.dto.PortalMemberResponseDTO;
import com.projecty.platform.modules.portal.api.dto.PortalMembershipResponseDTO;
import com.projecty.platform.modules.portal.api.dto.PortalSummaryResponseDTO;
import com.projecty.platform.modules.portal.internal.config.PortalProperties;
import com.projecty.platform.modules.portal.internal.constants.PortalConstants;
import com.projecty.platform.modules.portal.internal.dto.CurrentPortalResponseDTO;
import com.projecty.platform.modules.portal.internal.dto.PortalCreateDTO;
import com.projecty.platform.modules.portal.internal.dto.PortalResponseDTO;
import com.projecty.platform.modules.portal.internal.dto.PortalSettingsUpdateDTO;
import com.projecty.platform.modules.portal.internal.dto.SlugAvailabilityResponseDTO;
import com.projecty.platform.modules.portal.internal.entity.MemberStatus;
import com.projecty.platform.modules.portal.internal.entity.Portal;
import com.projecty.platform.modules.portal.internal.entity.PortalMember;
import com.projecty.platform.modules.portal.internal.entity.PortalProfile;
import com.projecty.platform.modules.portal.internal.helper.SlugRulesHelper;
import com.projecty.platform.modules.portal.internal.mapper.PortalMapper;
import com.projecty.platform.modules.portal.internal.repository.PortalMemberRepository;
import com.projecty.platform.modules.portal.internal.repository.PortalRepository;
import com.projecty.platform.modules.portal.internal.service.PortalService;
import com.projecty.platform.modules.shared.constants.AuditEventConstants;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalServiceImpl implements PortalService {

	private final PortalRepository portalRepository;

	private final PortalMemberRepository memberRepository;

	private final PortalMapper portalMapper;

	private final RequestPortalContext portalContext;

	private final AuditWriteService auditWriteService;

	private final PortalProperties properties;

	private final Clock clock;

	public PortalServiceImpl(PortalRepository portalRepository, PortalMemberRepository memberRepository,
			PortalMapper portalMapper, RequestPortalContext portalContext, AuditWriteService auditWriteService,
			PortalProperties properties, Clock clock) {
		this.portalRepository = portalRepository;
		this.memberRepository = memberRepository;
		this.portalMapper = portalMapper;
		this.portalContext = portalContext;
		this.auditWriteService = auditWriteService;
		this.properties = properties;
		this.clock = clock;
	}

	@Override
	@Transactional(readOnly = true)
	public List<UUID> findActivePortalIds(UUID userId) {
		return this.memberRepository.findActivePortalIds(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<PortalMembershipResponseDTO> findMemberships(UUID userId) {
		return this.memberRepository.findMemberships(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<PortalSummaryResponseDTO> findAllPortals() {
		return this.portalRepository.findAllSummaries();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<PortalSummaryResponseDTO> findPortal(UUID portalId) {
		return this.portalRepository.findSummary(portalId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<PortalMemberResponseDTO> findMembers(UUID portalId) {
		return this.memberRepository.findMembers(portalId);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isActiveMember(UUID portalId, UUID userId) {
		return this.memberRepository.existsByPortalIdAndUserIdAndStatus(portalId, userId, MemberStatus.ACTIVE);
	}

	@Override
	@Transactional
	public PortalResponseDTO create(UUID userId, PortalCreateDTO request) {
		String slug = request.slug();
		Optional<String> slugProblem = SlugRulesHelper.problem(slug);
		if (slugProblem.isPresent()) {
			throw new AppException(HttpStatus.BAD_REQUEST, PortalConstants.ERROR_SLUG_INVALID, slugProblem.get());
		}
		if (this.memberRepository.findActivePortalIds(userId).size() >= this.properties.maxPortalsPerUser()) {
			throw new AppException(HttpStatus.UNPROCESSABLE_CONTENT, PortalConstants.ERROR_PORTAL_LIMIT,
					"You can belong to at most " + this.properties.maxPortalsPerUser() + " portals.");
		}
		if (this.portalRepository.existsBySlug(slug)) {
			throw slugTaken(slug);
		}
		Instant now = this.clock.instant();
		try {
			Portal portal = this.portalRepository
				.saveAndFlush(new Portal(request.name().strip(), slug, userId, initialTimeZone(request.timeZone()), now));
			this.memberRepository.save(new PortalMember(portal.getId(), userId, PortalProfile.PORTAL_OWNER, now));
			return this.portalMapper.toResponse(portal, PortalProfile.PORTAL_OWNER);
		}
		catch (DataIntegrityViolationException ex) {
			// Another request took the slug between the check and the insert.
			throw slugTaken(slug);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public CurrentPortalResponseDTO getCurrent(UUID userId) {
		PortalMember member = currentMember(userId);
		return this.portalMapper.toCurrent(currentPortal(member), member.getProfile());
	}

	@Override
	@Transactional
	public CurrentPortalResponseDTO updateSettings(UUID userId, String userName, PortalSettingsUpdateDTO request) {
		PortalMember member = currentMember(userId);
		if (member.getProfile() != PortalProfile.PORTAL_OWNER) {
			throw new AppException(HttpStatus.FORBIDDEN, AppConstants.ERROR_FORBIDDEN,
					PortalConstants.MESSAGE_SETTINGS_FORBIDDEN);
		}
		validateSettings(request);
		Portal portal = currentPortal(member);
		String changes = describeChanges(portal, request);
		portal.updateSettings(request.timeZone(), request.dateFormat(), request.currency(), request.weekStart(),
				this.clock.instant());
		if (!changes.isEmpty()) {
			this.auditWriteService.record(new AuditEventCreateDTO(AuditEventConstants.CATEGORY_ADMIN,
					AuditEventConstants.ACTION_PORTAL_SETTINGS_CHANGED, AuditEventConstants.ACTOR_USER, userId,
					userName, portal.getId(), null, null, changes));
		}
		return this.portalMapper.toCurrent(portal, member.getProfile());
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
	 * The caller's active membership in the request's portal. Not a member → "not found", so the portal's
	 * existence isn't revealed (PTL-US-04).
	 */
	private PortalMember currentMember(UUID userId) {
		UUID portalId = this.portalContext.requirePortalId();
		return this.memberRepository.findByPortalIdAndUserIdAndStatus(portalId, userId, MemberStatus.ACTIVE)
			.orElseThrow(PortalServiceImpl::portalNotFound);
	}

	private Portal currentPortal(PortalMember member) {
		return this.portalRepository.findById(member.getPortalId()).orElseThrow(PortalServiceImpl::portalNotFound);
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
