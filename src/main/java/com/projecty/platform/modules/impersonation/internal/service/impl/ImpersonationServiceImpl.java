package com.projecty.platform.modules.impersonation.internal.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.common.config.AppProperties;
import com.projecty.platform.common.exception.AppException;
import com.projecty.platform.modules.account.api.UserLookUpService;
import com.projecty.platform.modules.account.api.dto.UserResponseDTO;
import com.projecty.platform.modules.admin.api.AdminLookUpService;
import com.projecty.platform.modules.admin.api.dto.AdminResponseDTO;
import com.projecty.platform.modules.audit.api.AuditWriteService;
import com.projecty.platform.modules.audit.api.dto.AuditEventCreateDTO;
import com.projecty.platform.modules.impersonation.internal.config.ImpersonationProperties;
import com.projecty.platform.modules.impersonation.internal.constants.ImpersonationConstants;
import com.projecty.platform.modules.impersonation.internal.dto.ImpersonationCreateDTO;
import com.projecty.platform.modules.impersonation.internal.dto.ImpersonationStartResponseDTO;
import com.projecty.platform.modules.impersonation.internal.entity.Impersonation;
import com.projecty.platform.modules.impersonation.internal.repository.ImpersonationRepository;
import com.projecty.platform.modules.impersonation.internal.service.ImpersonationService;
import com.projecty.platform.modules.portal.api.PortalLookUpService;
import com.projecty.platform.modules.session.api.SessionWriteService;
import com.projecty.platform.modules.session.api.dto.IssuedSessionDTO;
import com.projecty.platform.modules.session.api.dto.SupportSessionCreateDTO;
import com.projecty.platform.modules.shared.constants.AuditEventConstants;
import com.projecty.platform.modules.shared.helper.OpaqueTokenHelper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Composite service: owns impersonation requests. Checks the target through the portal and account modules,
 * names the admin through the admin module, opens and ends the session through the session module, and
 * writes every step to the audit log.
 */
@Service
public class ImpersonationServiceImpl implements ImpersonationService {

	private final ImpersonationRepository repository;

	private final PortalLookUpService portalLookUpService;

	private final UserLookUpService userLookUpService;

	private final AdminLookUpService adminLookUpService;

	private final SessionWriteService sessionWriteService;

	private final AuditWriteService auditWriteService;

	private final ImpersonationProperties properties;

	private final AppProperties appProperties;

	private final Clock clock;

	public ImpersonationServiceImpl(ImpersonationRepository repository, PortalLookUpService portalLookUpService,
			UserLookUpService userLookUpService, AdminLookUpService adminLookUpService,
			SessionWriteService sessionWriteService, AuditWriteService auditWriteService,
			ImpersonationProperties properties, AppProperties appProperties, Clock clock) {
		this.repository = repository;
		this.portalLookUpService = portalLookUpService;
		this.userLookUpService = userLookUpService;
		this.adminLookUpService = adminLookUpService;
		this.sessionWriteService = sessionWriteService;
		this.auditWriteService = auditWriteService;
		this.properties = properties;
		this.appProperties = appProperties;
		this.clock = clock;
	}

	@Override
	@Transactional
	public ImpersonationStartResponseDTO start(UUID adminId, ImpersonationCreateDTO request) {
		if (!ImpersonationConstants.ALLOWED_DURATION_MINUTES.contains(request.durationMinutes())) {
			throw invalid(ImpersonationConstants.MESSAGE_INVALID_DURATION);
		}
		if (!ImpersonationConstants.ALLOWED_REASONS.contains(request.reason())) {
			throw invalid(ImpersonationConstants.MESSAGE_INVALID_REASON);
		}
		UserResponseDTO target = eligibleTarget(request.portalId(), request.targetUserId())
			.orElseThrow(ImpersonationServiceImpl::notEligible);
		AdminResponseDTO admin = this.adminLookUpService.findById(adminId)
			.orElseThrow(ImpersonationServiceImpl::notEligible);

		String code = OpaqueTokenHelper.generate();
		Impersonation impersonation = this.repository.save(new Impersonation(adminId, request.portalId(),
				target.id(), request.reason(), request.comment().strip(), request.durationMinutes(),
				OpaqueTokenHelper.hash(code), this.clock.instant(), this.properties.codeTtl()));

		String detail = "Target user: %s (%s); Duration: %d minutes; Reason: %s; Comment: %s; Expires: %s"
			.formatted(target.fullName(), target.email(), request.durationMinutes(), request.reason(),
					impersonation.getComment(),
					ImpersonationConstants.AUDIT_TIME_FORMAT.format(impersonation.getExpiresAt()));
		audit(AuditEventConstants.ACTION_IMPERSONATION_REQUESTED, impersonation, admin.fullName(), target.fullName(),
				detail);

		String launchUrl = "%s/%s?code=%s".formatted(this.appProperties.frontendUrl(),
				ImpersonationConstants.LAUNCH_PAGE, code);
		return new ImpersonationStartResponseDTO(impersonation.getId(), launchUrl, impersonation.getCodeExpiresAt(),
				impersonation.getExpiresAt());
	}

	@Override
	@Transactional
	public IssuedSessionDTO startSupportSession(String code, String userAgent, String ip) {
		Instant now = this.clock.instant();
		Impersonation impersonation = Optional.ofNullable(code)
			.filter((value) -> !value.isBlank())
			.flatMap((value) -> this.repository.findByCodeHash(OpaqueTokenHelper.hash(value)))
			.filter((found) -> found.canRedeem(now))
			.orElseThrow(ImpersonationServiceImpl::linkInvalid);
		UserResponseDTO target = eligibleTarget(impersonation.getPortalId(), impersonation.getTargetUserId())
			.orElseThrow(ImpersonationServiceImpl::linkInvalid);
		String adminName = adminName(impersonation.getAdminUserId());
		impersonation.redeem(now);
		audit(AuditEventConstants.ACTION_IMPERSONATION_STARTED, impersonation, adminName, target.fullName(),
				"Support session opened; ends "
						+ ImpersonationConstants.AUDIT_TIME_FORMAT.format(impersonation.getExpiresAt()));
		return this.sessionWriteService.openSupportSession(new SupportSessionCreateDTO(target.id(),
				impersonation.getId(), impersonation.getAdminUserId(), adminName, impersonation.getExpiresAt(),
				userAgent, ip));
	}

	@Override
	@Transactional
	public void endSupportSession(String refreshToken) {
		UUID impersonationId = this.sessionWriteService.endSupportSession(refreshToken)
			.orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, ImpersonationConstants.ERROR_NOT_IMPERSONATING,
					ImpersonationConstants.MESSAGE_NOT_IMPERSONATING));
		markEnded(impersonationId, ImpersonationConstants.END_REASON_BY_ADMIN);
	}

	@Override
	@Transactional
	public void markEnded(UUID impersonationId, String reason) {
		this.repository.findById(impersonationId)
			.filter((impersonation) -> impersonation.end(this.clock.instant()))
			.ifPresent((impersonation) -> audit(AuditEventConstants.ACTION_IMPERSONATION_ENDED, impersonation,
					adminName(impersonation.getAdminUserId()), targetName(impersonation.getTargetUserId()),
					reason));
	}

	/**
	 * The target must be an active identity and an active member of the tenant.
	 */
	private Optional<UserResponseDTO> eligibleTarget(UUID portalId, UUID userId) {
		if (!this.portalLookUpService.isActiveMember(portalId, userId)) {
			return Optional.empty();
		}
		return this.userLookUpService.findById(userId).filter(UserResponseDTO::active);
	}

	private String adminName(UUID adminId) {
		return this.adminLookUpService.findById(adminId).map(AdminResponseDTO::fullName).orElse(null);
	}

	private String targetName(UUID userId) {
		return this.userLookUpService.findById(userId).map(UserResponseDTO::fullName).orElse(null);
	}

	private void audit(String action, Impersonation impersonation, String adminName, String targetName,
			String detail) {
		this.auditWriteService.record(new AuditEventCreateDTO(AuditEventConstants.CATEGORY_SECURITY, action,
				AuditEventConstants.ACTOR_ADMIN, impersonation.getAdminUserId(), adminName,
				impersonation.getPortalId(), impersonation.getTargetUserId(), targetName, detail));
	}

	private static AppException invalid(String message) {
		return new AppException(HttpStatus.BAD_REQUEST, ImpersonationConstants.ERROR_INVALID_REQUEST, message);
	}

	private static AppException notEligible() {
		return new AppException(HttpStatus.UNPROCESSABLE_CONTENT, ImpersonationConstants.ERROR_TARGET_NOT_ELIGIBLE,
				ImpersonationConstants.MESSAGE_TARGET_NOT_ELIGIBLE);
	}

	private static AppException linkInvalid() {
		return new AppException(HttpStatus.BAD_REQUEST, ImpersonationConstants.ERROR_LINK_INVALID,
				ImpersonationConstants.MESSAGE_LINK_INVALID);
	}

}
