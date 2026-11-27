package com.projecty.platform.modules.member.internal.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.projecty.platform.common.exception.AppException;
import com.projecty.platform.common.service.RequestPortalContext;
import com.projecty.platform.modules.account.api.UserLookUpService;
import com.projecty.platform.modules.account.api.dto.UserResponseDTO;
import com.projecty.platform.modules.audit.api.AuditWriteService;
import com.projecty.platform.modules.audit.api.dto.AuditEventCreateDTO;
import com.projecty.platform.modules.outbox.api.OutboxWriteService;
import com.projecty.platform.modules.outbox.api.dto.OutboxEventCreateDTO;
import com.projecty.platform.modules.member.internal.config.MemberProperties;
import com.projecty.platform.modules.member.internal.constants.MemberConstants;
import com.projecty.platform.modules.member.internal.dto.MemberResponseDTO;
import com.projecty.platform.modules.member.internal.dto.MyMembershipResponseDTO;
import com.projecty.platform.modules.member.internal.dto.ProfileResponseDTO;
import com.projecty.platform.modules.member.internal.entity.MemberStatus;
import com.projecty.platform.modules.member.internal.entity.PortalMember;
import com.projecty.platform.modules.member.internal.entity.Profile;
import com.projecty.platform.modules.member.internal.helper.MemberAccessCache;
import com.projecty.platform.modules.member.internal.helper.MemberResponseCache;
import com.projecty.platform.modules.member.internal.mapper.MemberMapper;
import com.projecty.platform.modules.member.internal.repository.PortalMemberRepository;
import com.projecty.platform.modules.member.internal.repository.ProfileRepository;
import com.projecty.platform.modules.member.internal.service.MemberService;
import com.projecty.platform.modules.portal.api.PortalLookUpService;
import com.projecty.platform.modules.shared.constants.AuditEventConstants;
import com.projecty.platform.modules.shared.constants.OutboxEventConstants;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Composite service: owns members and profiles; names people through the account module, finds the portal
 * owner through the portal module, and audits every change.
 */
@Service
public class MemberServiceImpl implements MemberService {

	private final PortalMemberRepository memberRepository;

	private final ProfileRepository profileRepository;

	private final UserLookUpService userLookUpService;

	private final PortalLookUpService portalLookUpService;

	private final AuditWriteService auditWriteService;

	private final OutboxWriteService outboxWriteService;

	private final RequestPortalContext portalContext;

	private final MemberMapper mapper;

	private final MemberProperties properties;

	private final MemberAccessCache accessCache;

	private final MemberResponseCache responseCache;

	private final Clock clock;

	public MemberServiceImpl(PortalMemberRepository memberRepository, ProfileRepository profileRepository,
			UserLookUpService userLookUpService, PortalLookUpService portalLookUpService,
			AuditWriteService auditWriteService, OutboxWriteService outboxWriteService,
			RequestPortalContext portalContext, MemberMapper mapper, MemberProperties properties,
			MemberAccessCache accessCache, MemberResponseCache responseCache, Clock clock) {
		this.memberRepository = memberRepository;
		this.profileRepository = profileRepository;
		this.userLookUpService = userLookUpService;
		this.portalLookUpService = portalLookUpService;
		this.auditWriteService = auditWriteService;
		this.outboxWriteService = outboxWriteService;
		this.portalContext = portalContext;
		this.mapper = mapper;
		this.properties = properties;
		this.accessCache = accessCache;
		this.responseCache = responseCache;
		this.clock = clock;
	}

	@Override
	@Transactional(readOnly = true)
	public MyMembershipResponseDTO getMine(UUID userId) {
		UUID portalId = this.portalContext.requirePortalId();
		return this.responseCache.mine(portalId, userId, () -> {
			PortalMember member = findMember(portalId, userId);
			return this.mapper.toMine(profile(member.getProfileId()), userId.equals(ownerOf(portalId)));
		});
	}

	@Override
	@Transactional(readOnly = true)
	public List<MemberResponseDTO> listMembers() {
		UUID portalId = this.portalContext.requirePortalId();
		return this.responseCache.members(portalId, () -> loadMembers(portalId));
	}

	private List<MemberResponseDTO> loadMembers(UUID portalId) {
		List<PortalMember> members = this.memberRepository.findByPortalIdOrderByJoinedAt(portalId);
		Map<UUID, UserResponseDTO> users = this.userLookUpService
			.findByIds(members.stream().map(PortalMember::getUserId).toList())
			.stream()
			.collect(Collectors.toMap(UserResponseDTO::id, Function.identity()));
		Map<UUID, Profile> profiles = this.profileRepository.findByPortalIdOrderByCreatedAtAscNameAsc(portalId)
			.stream()
			.collect(Collectors.toMap(Profile::getId, Function.identity()));
		UUID owner = ownerOf(portalId);
		return members.stream()
			.filter((m) -> users.containsKey(m.getUserId()))
			.map((m) -> this.mapper.toMember(m, users.get(m.getUserId()), profiles.get(m.getProfileId()),
					m.getUserId().equals(owner)))
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProfileResponseDTO> listProfiles() {
		UUID portalId = this.portalContext.requirePortalId();
		return this.responseCache.profiles(portalId, () -> this.profileRepository
			.findByPortalIdOrderByCreatedAtAscNameAsc(portalId)
			.stream()
			.map(this.mapper::toProfile)
			.toList());
	}

	@Override
	@Transactional
	public MemberResponseDTO changeProfile(UUID actorId, String actorName, UUID userId, UUID profileId) {
		UUID portalId = this.portalContext.requirePortalId();
		PortalMember member = findMember(portalId, userId);
		Profile current = profile(member.getProfileId());
		Profile next = this.profileRepository.findByIdAndPortalId(profileId, portalId)
			.orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, MemberConstants.ERROR_PROFILE_NOT_FOUND,
					MemberConstants.MESSAGE_PROFILE_NOT_FOUND));
		if (current.getId().equals(next.getId())) {
			return toMember(member, portalId);
		}
		if (userId.equals(ownerOf(portalId))) {
			throw ownerLocked();
		}
		if (isAdmin(current) && member.isActive()) {
			requireAnotherAdmin(portalId, current);
		}
		member.changeProfile(next.getId());
		audit(AuditEventConstants.ACTION_USER_PROFILE_CHANGED, actorId, actorName, portalId, userId,
				"Profile " + current.getName() + " → " + next.getName());
		publish(OutboxEventConstants.EVENT_USER_PROFILE_CHANGED, portalId, userId, actorId, actorName,
				Map.of("portalId", portalId, "userId", userId, "actorUserId", actorId, "fromProfileId",
						current.getId(), "toProfileId", next.getId(), "profileKey", next.getProfileKey(),
						"permissions", permissionsOf(next)));
		return toMember(member, portalId);
	}

	@Override
	@Transactional
	public MemberResponseDTO deactivate(UUID actorId, String actorName, UUID userId) {
		UUID portalId = this.portalContext.requirePortalId();
		PortalMember member = findMember(portalId, userId);
		if (!member.isActive()) {
			return toMember(member, portalId);
		}
		if (userId.equals(ownerOf(portalId))) {
			throw ownerLocked();
		}
		Profile profile = profile(member.getProfileId());
		if (isAdmin(profile)) {
			requireAnotherAdmin(portalId, profile);
		}
		member.deactivate(this.clock.instant());
		audit(AuditEventConstants.ACTION_USER_DEACTIVATED, actorId, actorName, portalId, userId, null);
		publish(OutboxEventConstants.EVENT_USER_DEACTIVATED, portalId, userId, actorId, actorName,
				Map.of("portalId", portalId, "userId", userId, "actorUserId", actorId));
		return toMember(member, portalId);
	}

	@Override
	@Transactional
	public MemberResponseDTO reactivate(UUID actorId, String actorName, UUID userId) {
		UUID portalId = this.portalContext.requirePortalId();
		PortalMember member = findMember(portalId, userId);
		if (!member.isActive()) {
			requireRoomForAnotherPortal(userId);
			member.reactivate();
			Profile profile = profile(member.getProfileId());
			audit(AuditEventConstants.ACTION_USER_REACTIVATED, actorId, actorName, portalId, userId, null);
			publish(OutboxEventConstants.EVENT_USER_ACTIVATED, portalId, userId, actorId, actorName,
					Map.of("portalId", portalId, "userId", userId, "actorUserId", actorId, "profileId",
							profile.getId(), "profileKey", profile.getProfileKey(), "permissions",
							permissionsOf(profile)));
		}
		return toMember(member, portalId);
	}

	@Override
	@Transactional
	public void setUpNewPortal(UUID portalId, UUID ownerUserId) {
		requireRoomForAnotherPortal(ownerUserId);
		Instant now = this.clock.instant();
		Profile adminProfile = null;
		for (MemberConstants.ProfileDefinition definition : MemberConstants.SYSTEM_PROFILES) {
			Profile profile = this.profileRepository.save(new Profile(portalId, definition.key(), definition.name(),
					definition.description(), definition.permissions(), now));
			if (MemberConstants.PROFILE_ADMIN.equals(definition.key())) {
				adminProfile = profile;
			}
		}
		this.memberRepository.save(new PortalMember(portalId, ownerUserId, adminProfile.getId(), now));
		// The owner joins like anyone else, so other services learn the owner's profile and permissions too.
		publish(OutboxEventConstants.EVENT_USER_ACTIVATED, portalId, ownerUserId, ownerUserId, null,
				Map.of("portalId", portalId, "userId", ownerUserId, "profileId", adminProfile.getId(), "profileKey",
						adminProfile.getProfileKey(), "permissions", permissionsOf(adminProfile)));
	}

	@Override
	@Transactional
	public void addMember(UUID portalId, UUID userId, UUID profileId) {
		Optional<PortalMember> existing = this.memberRepository.findByPortalIdAndUserId(portalId, userId);
		if (existing.isPresent() && existing.get().isActive()) {
			throw new AppException(HttpStatus.CONFLICT, MemberConstants.ERROR_ALREADY_MEMBER,
					MemberConstants.MESSAGE_ALREADY_MEMBER);
		}
		requireRoomForAnotherPortal(userId);
		if (existing.isPresent()) {
			existing.get().changeProfile(profileId);
			existing.get().reactivate();
		}
		else {
			this.memberRepository.save(new PortalMember(portalId, userId, profileId, this.clock.instant()));
		}
		Profile profile = profile(profileId);
		publish(OutboxEventConstants.EVENT_USER_ACTIVATED, portalId, userId, userId, null,
				Map.of("portalId", portalId, "userId", userId, "profileId", profileId, "profileKey",
						profile.getProfileKey(), "permissions", permissionsOf(profile)));
	}

	/**
	 * PTL-BR-10: a user can be in at most {@code maxPortalsPerUser} portals.
	 */
	private void requireRoomForAnotherPortal(UUID userId) {
		if (this.memberRepository.findActivePortalIds(userId).size() >= this.properties.maxPortalsPerUser()) {
			throw new AppException(HttpStatus.UNPROCESSABLE_CONTENT, MemberConstants.ERROR_PORTAL_LIMIT,
					"You can belong to at most " + this.properties.maxPortalsPerUser() + " portals.");
		}
	}

	/**
	 * USR-BR-06: the last active Admin can't be demoted or deactivated.
	 */
	private void requireAnotherAdmin(UUID portalId, Profile adminProfile) {
		long admins = this.memberRepository.countByPortalIdAndProfileIdAndStatus(portalId, adminProfile.getId(),
				MemberStatus.ACTIVE);
		if (admins <= 1) {
			throw new AppException(HttpStatus.UNPROCESSABLE_CONTENT, MemberConstants.ERROR_LAST_ADMIN,
					MemberConstants.MESSAGE_LAST_ADMIN);
		}
	}

	/** Sorted, so the same profile always produces the same event payload. */
	private static List<String> permissionsOf(Profile profile) {
		return profile.getPermissions().stream().sorted().toList();
	}

	private static boolean isAdmin(Profile profile) {
		return MemberConstants.PROFILE_ADMIN.equals(profile.getProfileKey());
	}

	private PortalMember findMember(UUID portalId, UUID userId) {
		return this.memberRepository.findByPortalIdAndUserId(portalId, userId)
			.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, MemberConstants.ERROR_MEMBER_NOT_FOUND,
					MemberConstants.MESSAGE_MEMBER_NOT_FOUND));
	}

	private Profile profile(UUID profileId) {
		return this.profileRepository.findById(profileId).orElseThrow();
	}

	private UUID ownerOf(UUID portalId) {
		return this.portalLookUpService.findPortal(portalId).orElseThrow().ownerUserId();
	}

	private MemberResponseDTO toMember(PortalMember member, UUID portalId) {
		UserResponseDTO user = this.userLookUpService.findById(member.getUserId()).orElseThrow();
		return this.mapper.toMember(member, user, profile(member.getProfileId()),
				member.getUserId().equals(ownerOf(portalId)));
	}

	private void audit(String action, UUID actorId, String actorName, UUID portalId, UUID targetUserId,
			String detail) {
		String targetName = this.userLookUpService.findById(targetUserId).map(UserResponseDTO::fullName).orElse(null);
		this.auditWriteService.record(new AuditEventCreateDTO(AuditEventConstants.CATEGORY_ADMIN, action,
				AuditEventConstants.ACTOR_USER, actorId, actorName, portalId, targetUserId, targetName, detail));
	}

	private void publish(String eventType, UUID portalId, UUID userId, UUID actorId, String actorName,
			Map<String, Object> payload) {
		this.outboxWriteService.publish(new OutboxEventCreateDTO(portalId, OutboxEventConstants.AGGREGATE_MEMBER,
				userId, eventType, actorId, actorName, payload));
		// Every member change publishes, so this is also the one place that drops the cached access answer.
		this.accessCache.evictAfterCommit(portalId, userId);
		this.responseCache.memberChanged(portalId, userId);
	}

	private static AppException ownerLocked() {
		return new AppException(HttpStatus.UNPROCESSABLE_CONTENT, MemberConstants.ERROR_OWNER_LOCKED,
				MemberConstants.MESSAGE_OWNER_LOCKED);
	}

}
