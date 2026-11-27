package com.projecty.platform.modules.member.internal.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.common.exception.AppException;
import com.projecty.platform.common.service.RequestPortalContext;
import com.projecty.platform.modules.account.api.UserLookUpService;
import com.projecty.platform.modules.account.api.UserWriteService;
import com.projecty.platform.modules.account.api.dto.UserResponseDTO;
import com.projecty.platform.modules.audit.api.AuditWriteService;
import com.projecty.platform.modules.audit.api.dto.AuditEventCreateDTO;
import com.projecty.platform.modules.outbox.api.OutboxWriteService;
import com.projecty.platform.modules.outbox.api.dto.OutboxEventCreateDTO;
import com.projecty.platform.modules.member.internal.config.MemberProperties;
import com.projecty.platform.modules.member.internal.constants.MemberConstants;
import com.projecty.platform.modules.member.internal.dto.AcceptedInvitationResponseDTO;
import com.projecty.platform.modules.member.internal.dto.InvitationAcceptNewDTO;
import com.projecty.platform.modules.member.internal.dto.InvitationCreateDTO;
import com.projecty.platform.modules.member.internal.dto.InvitationPreviewResponseDTO;
import com.projecty.platform.modules.member.internal.dto.InvitationResponseDTO;
import com.projecty.platform.modules.member.internal.entity.Invitation;
import com.projecty.platform.modules.member.internal.entity.InvitationStatus;
import com.projecty.platform.modules.member.internal.entity.MemberStatus;
import com.projecty.platform.modules.member.internal.entity.Profile;
import com.projecty.platform.modules.member.internal.event.InvitationIssuedEvent;
import com.projecty.platform.modules.member.internal.helper.MemberResponseCache;
import com.projecty.platform.modules.member.internal.mapper.MemberMapper;
import com.projecty.platform.modules.member.internal.repository.InvitationRepository;
import com.projecty.platform.modules.member.internal.repository.PortalMemberRepository;
import com.projecty.platform.modules.member.internal.repository.ProfileRepository;
import com.projecty.platform.modules.member.internal.service.InvitationService;
import com.projecty.platform.modules.member.internal.service.MemberService;
import com.projecty.platform.modules.portal.api.PortalLookUpService;
import com.projecty.platform.modules.portal.api.dto.PortalSummaryResponseDTO;
import com.projecty.platform.modules.shared.constants.AuditEventConstants;
import com.projecty.platform.modules.shared.constants.OutboxEventConstants;
import com.projecty.platform.modules.shared.helper.OpaqueTokenHelper;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Composite service: owns invitations. Creates or finds identities through the account module, joins them
 * through {@link MemberService}, emails the link after commit, and audits every step.
 */
@Service
public class InvitationServiceImpl implements InvitationService {

	private final InvitationRepository invitationRepository;

	private final ProfileRepository profileRepository;

	private final PortalMemberRepository memberRepository;

	private final MemberService memberService;

	private final UserLookUpService userLookUpService;

	private final UserWriteService userWriteService;

	private final PortalLookUpService portalLookUpService;

	private final AuditWriteService auditWriteService;

	private final OutboxWriteService outboxWriteService;

	private final RequestPortalContext portalContext;

	private final ApplicationEventPublisher events;

	private final MemberMapper mapper;

	private final MemberProperties properties;

	private final MemberResponseCache responseCache;

	private final Clock clock;

	public InvitationServiceImpl(InvitationRepository invitationRepository, ProfileRepository profileRepository,
			PortalMemberRepository memberRepository, MemberService memberService, UserLookUpService userLookUpService,
			UserWriteService userWriteService, PortalLookUpService portalLookUpService,
			AuditWriteService auditWriteService, OutboxWriteService outboxWriteService,
			RequestPortalContext portalContext, ApplicationEventPublisher events,
			MemberMapper mapper, MemberProperties properties, MemberResponseCache responseCache, Clock clock) {
		this.invitationRepository = invitationRepository;
		this.profileRepository = profileRepository;
		this.memberRepository = memberRepository;
		this.memberService = memberService;
		this.userLookUpService = userLookUpService;
		this.userWriteService = userWriteService;
		this.portalLookUpService = portalLookUpService;
		this.auditWriteService = auditWriteService;
		this.outboxWriteService = outboxWriteService;
		this.portalContext = portalContext;
		this.events = events;
		this.mapper = mapper;
		this.properties = properties;
		this.responseCache = responseCache;
		this.clock = clock;
	}

	@Override
	@Transactional(readOnly = true)
	public List<InvitationResponseDTO> listPending() {
		UUID portalId = this.portalContext.requirePortalId();
		return this.responseCache.invitations(portalId, () -> this.invitationRepository
			.findByPortalIdAndStatusOrderByCreatedAtDesc(portalId, InvitationStatus.PENDING)
			.stream()
			.map(this::toResponse)
			.toList());
	}

	@Override
	@Transactional
	public InvitationResponseDTO invite(UUID actorId, String actorName, InvitationCreateDTO request) {
		UUID portalId = this.portalContext.requirePortalId();
		String email = request.email().strip();
		Profile profile = this.profileRepository.findByIdAndPortalId(request.profileId(), portalId)
			.orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, MemberConstants.ERROR_PROFILE_NOT_FOUND,
					MemberConstants.MESSAGE_PROFILE_NOT_FOUND));
		Optional<UserResponseDTO> existingUser = this.userLookUpService.findByEmail(email);
		if (existingUser.isPresent() && this.memberRepository.existsByPortalIdAndUserIdAndStatus(portalId,
				existingUser.get().id(), MemberStatus.ACTIVE)) {
			throw new AppException(HttpStatus.CONFLICT, MemberConstants.ERROR_ALREADY_MEMBER,
					MemberConstants.MESSAGE_ALREADY_MEMBER);
		}
		this.invitationRepository.findOpen(portalId, email, InvitationStatus.PENDING).forEach(Invitation::revoke);

		String token = OpaqueTokenHelper.generate();
		Invitation invitation = this.invitationRepository.save(new Invitation(portalId, email, profile.getId(), actorId,
				OpaqueTokenHelper.hash(token), this.clock.instant(), this.properties.invitationTtl()));
		String portalName = portal(portalId).name();
		this.events.publishEvent(new InvitationIssuedEvent(email, portalName, actorName, profile.getName(), token));
		audit(AuditEventConstants.ACTION_USER_INVITED, actorId, actorName, portalId, null, email,
				"Invited as " + profile.getName());
		this.outboxWriteService.publish(new OutboxEventCreateDTO(portalId, OutboxEventConstants.AGGREGATE_INVITATION,
				invitation.getId(), OutboxEventConstants.EVENT_USER_INVITED, actorId, actorName, Map.of("portalId", portalId,
						"invitationId", invitation.getId(), "profileId", profile.getId(), "invitedByUserId", actorId)));
		this.responseCache.invitationsChanged(portalId);
		return toResponse(invitation);
	}

	@Override
	@Transactional
	public void revoke(UUID actorId, String actorName, UUID invitationId) {
		UUID portalId = this.portalContext.requirePortalId();
		Invitation invitation = this.invitationRepository.findByIdAndPortalId(invitationId, portalId)
			.filter((i) -> i.getStatus() == InvitationStatus.PENDING)
			.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, MemberConstants.ERROR_INVITATION_NOT_FOUND,
					MemberConstants.MESSAGE_INVITATION_NOT_FOUND));
		invitation.revoke();
		audit(AuditEventConstants.ACTION_INVITATION_REVOKED, actorId, actorName, portalId, null,
				invitation.getEmail(), null);
		this.outboxWriteService.publish(new OutboxEventCreateDTO(portalId, OutboxEventConstants.AGGREGATE_INVITATION,
				invitation.getId(), OutboxEventConstants.EVENT_INVITATION_REVOKED, actorId, actorName,
				Map.of("portalId", portalId, "invitationId", invitation.getId(), "revokedByUserId", actorId)));
		this.responseCache.invitationsChanged(portalId);
	}

	@Override
	@Transactional
	public InvitationPreviewResponseDTO preview(String token) {
		Invitation invitation = findByToken(token).orElseThrow(InvitationServiceImpl::invalid);
		Profile profile = this.profileRepository.findById(invitation.getProfileId()).orElseThrow();
		return new InvitationPreviewResponseDTO(portal(invitation.getPortalId()).name(), invitation.getEmail(),
				profile.getName(), userName(invitation.getInvitedByUserId()), invitation.isUsable(this.clock.instant()),
				this.userLookUpService.findByEmail(invitation.getEmail()).isPresent());
	}

	@Override
	@Transactional
	public AcceptedInvitationResponseDTO acceptAsNewUser(InvitationAcceptNewDTO request) {
		Invitation invitation = usable(request.token());
		if (this.userLookUpService.findByEmail(invitation.getEmail()).isPresent()) {
			throw new AppException(HttpStatus.CONFLICT, MemberConstants.ERROR_ACCOUNT_EXISTS,
					MemberConstants.MESSAGE_ACCOUNT_EXISTS);
		}
		UserResponseDTO user = this.userWriteService.createVerifiedUser(invitation.getEmail(), request.fullName(),
				request.password());
		return join(invitation, user);
	}

	@Override
	@Transactional
	public AcceptedInvitationResponseDTO accept(UUID userId, String token) {
		Invitation invitation = usable(token);
		UserResponseDTO user = this.userLookUpService.findById(userId).orElseThrow();
		if (!user.email().equalsIgnoreCase(invitation.getEmail())) {
			throw new AppException(HttpStatus.FORBIDDEN, MemberConstants.ERROR_INVITATION_EMAIL_MISMATCH,
					MemberConstants.MESSAGE_INVITATION_EMAIL_MISMATCH);
		}
		return join(invitation, user);
	}

	private AcceptedInvitationResponseDTO join(Invitation invitation, UserResponseDTO user) {
		this.memberService.addMember(invitation.getPortalId(), user.id(), invitation.getProfileId());
		invitation.accept(user.id(), this.clock.instant());
		this.responseCache.invitationsChanged(invitation.getPortalId());
		this.auditWriteService.record(new AuditEventCreateDTO(AuditEventConstants.CATEGORY_ADMIN,
				AuditEventConstants.ACTION_INVITATION_ACCEPTED, AuditEventConstants.ACTOR_USER, user.id(),
				user.fullName(), invitation.getPortalId(), user.id(), user.fullName(), null));
		PortalSummaryResponseDTO portal = portal(invitation.getPortalId());
		return new AcceptedInvitationResponseDTO(portal.id(), portal.slug());
	}

	private Invitation usable(String token) {
		Instant now = this.clock.instant();
		return findByToken(token).filter((i) -> i.isUsable(now)).orElseThrow(InvitationServiceImpl::invalid);
	}

	private Optional<Invitation> findByToken(String token) {
		if (token == null || token.isBlank()) {
			return Optional.empty();
		}
		return this.invitationRepository.findByTokenHash(OpaqueTokenHelper.hash(token));
	}

	private InvitationResponseDTO toResponse(Invitation invitation) {
		Profile profile = this.profileRepository.findById(invitation.getProfileId()).orElseThrow();
		return this.mapper.toInvitation(invitation, profile, userName(invitation.getInvitedByUserId()));
	}

	private PortalSummaryResponseDTO portal(UUID portalId) {
		return this.portalLookUpService.findPortal(portalId).orElseThrow();
	}

	private String userName(UUID userId) {
		return this.userLookUpService.findById(userId).map(UserResponseDTO::fullName).orElse(null);
	}

	private void audit(String action, UUID actorId, String actorName, UUID portalId, UUID targetUserId,
			String targetName, String detail) {
		this.auditWriteService.record(new AuditEventCreateDTO(AuditEventConstants.CATEGORY_ADMIN, action,
				AuditEventConstants.ACTOR_USER, actorId, actorName, portalId, targetUserId, targetName, detail));
	}

	private static AppException invalid() {
		return new AppException(HttpStatus.BAD_REQUEST, MemberConstants.ERROR_INVITATION_INVALID,
				MemberConstants.MESSAGE_INVITATION_INVALID);
	}

}
