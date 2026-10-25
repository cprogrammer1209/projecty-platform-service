package com.projecty.platform.modules.portal.internal.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.projecty.platform.common.exception.AppException;
import com.projecty.platform.modules.portal.api.dto.PortalMembershipResponseDTO;
import com.projecty.platform.modules.portal.internal.config.PortalProperties;
import com.projecty.platform.modules.portal.internal.constants.PortalConstants;
import com.projecty.platform.modules.portal.internal.dto.PortalCreateDTO;
import com.projecty.platform.modules.portal.internal.dto.PortalResponseDTO;
import com.projecty.platform.modules.portal.internal.dto.SlugAvailabilityResponseDTO;
import com.projecty.platform.modules.portal.internal.entity.Portal;
import com.projecty.platform.modules.portal.internal.entity.PortalMember;
import com.projecty.platform.modules.portal.internal.entity.PortalProfile;
import com.projecty.platform.modules.portal.internal.helper.SlugRulesHelper;
import com.projecty.platform.modules.portal.internal.mapper.PortalMapper;
import com.projecty.platform.modules.portal.internal.repository.PortalMemberRepository;
import com.projecty.platform.modules.portal.internal.repository.PortalRepository;
import com.projecty.platform.modules.portal.internal.service.PortalService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalServiceImpl implements PortalService {

	private final PortalRepository portalRepository;

	private final PortalMemberRepository memberRepository;

	private final PortalMapper portalMapper;

	private final PortalProperties properties;

	private final Clock clock;

	public PortalServiceImpl(PortalRepository portalRepository, PortalMemberRepository memberRepository,
			PortalMapper portalMapper, PortalProperties properties, Clock clock) {
		this.portalRepository = portalRepository;
		this.memberRepository = memberRepository;
		this.portalMapper = portalMapper;
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
			Portal portal = this.portalRepository.saveAndFlush(new Portal(request.name().strip(), slug, userId, now));
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
