package com.projecty.platform.modules.admin.internal.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.common.exception.AppException;
import com.projecty.platform.modules.admin.api.dto.AdminResponseDTO;
import com.projecty.platform.modules.admin.internal.config.AdminProperties;
import com.projecty.platform.modules.admin.internal.constants.AdminConstants;
import com.projecty.platform.modules.admin.internal.dto.AdminSessionTokensDTO;
import com.projecty.platform.modules.admin.internal.entity.AdminSession;
import com.projecty.platform.modules.admin.internal.entity.AdminUser;
import com.projecty.platform.modules.admin.internal.mapper.AdminMapper;
import com.projecty.platform.modules.admin.internal.repository.AdminSessionRepository;
import com.projecty.platform.modules.admin.internal.repository.AdminUserRepository;
import com.projecty.platform.modules.admin.internal.service.AdminUserService;
import com.projecty.platform.modules.audit.api.AuditWriteService;
import com.projecty.platform.modules.audit.api.dto.AuditEventCreateDTO;
import com.projecty.platform.modules.shared.constants.AuditEventConstants;
import com.projecty.platform.modules.shared.helper.OpaqueTokenHelper;
import com.projecty.platform.modules.token.api.TokenWriteService;
import com.projecty.platform.modules.token.api.dto.AdminAccessTokenClaimsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Composite service: owns admin identities and sessions; signs tokens through the token module and records
 * sign-ins in the audit log.
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

	private static final Logger logger = LoggerFactory.getLogger(AdminUserServiceImpl.class);

	private final AdminUserRepository userRepository;

	private final AdminSessionRepository sessionRepository;

	private final TokenWriteService tokenWriteService;

	private final AuditWriteService auditWriteService;

	private final PasswordEncoder passwordEncoder;

	private final AdminMapper mapper;

	private final AdminProperties properties;

	private final Clock clock;

	private final String timingDummyHash;

	public AdminUserServiceImpl(AdminUserRepository userRepository, AdminSessionRepository sessionRepository,
			TokenWriteService tokenWriteService, AuditWriteService auditWriteService, PasswordEncoder passwordEncoder,
			AdminMapper mapper, AdminProperties properties, Clock clock) {
		this.userRepository = userRepository;
		this.sessionRepository = sessionRepository;
		this.tokenWriteService = tokenWriteService;
		this.auditWriteService = auditWriteService;
		this.passwordEncoder = passwordEncoder;
		this.mapper = mapper;
		this.properties = properties;
		this.clock = clock;
		this.timingDummyHash = passwordEncoder.encode(AdminConstants.TIMING_DUMMY_PASSWORD);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<AdminResponseDTO> findById(UUID adminId) {
		return this.userRepository.findById(adminId).map(this.mapper::toResponse);
	}

	/**
	 * Not rolled back on failure, so failed attempts are still counted.
	 */
	@Override
	@Transactional(noRollbackFor = AppException.class)
	public AdminSessionTokensDTO login(String email, String password, String userAgent, String ip) {
		Instant now = this.clock.instant();
		AdminUser admin = this.userRepository.findByEmail(email.strip()).orElse(null);
		if (admin == null) {
			this.passwordEncoder.matches(password, this.timingDummyHash);
			throw invalidCredentials();
		}
		if (admin.isLocked(now)) {
			throw locked(admin);
		}
		if (!this.passwordEncoder.matches(password, admin.getPasswordHash())) {
			admin.recordFailedLogin(now, this.properties.lockoutMaxAttempts(), this.properties.lockoutWindow(),
					this.properties.lockoutDuration());
			throw admin.isLocked(now) ? locked(admin) : invalidCredentials();
		}
		if (!admin.isActive()) {
			throw new AppException(HttpStatus.FORBIDDEN, AdminConstants.ERROR_ACCOUNT_DISABLED,
					AdminConstants.MESSAGE_ACCOUNT_DISABLED);
		}
		admin.recordSuccessfulLogin(now);
		String refreshToken = OpaqueTokenHelper.generate();
		AdminSession session = this.sessionRepository.save(new AdminSession(admin.getId(),
				OpaqueTokenHelper.hash(refreshToken), truncate(userAgent), ip, now, this.properties.idleTimeout(),
				this.properties.absoluteTimeout()));
		this.auditWriteService.record(new AuditEventCreateDTO(AuditEventConstants.CATEGORY_SECURITY,
				AuditEventConstants.ACTION_ADMIN_SIGNED_IN, AuditEventConstants.ACTOR_ADMIN, admin.getId(),
				admin.getFullName(), null, null, null, null));
		return issue(admin, session, refreshToken);
	}

	@Override
	@Transactional(noRollbackFor = AppException.class)
	public AdminSessionTokensDTO refresh(String refreshToken) {
		Instant now = this.clock.instant();
		AdminSession session = findSession(refreshToken).orElseThrow(AdminUserServiceImpl::sessionExpired);
		if (!session.isUsable(now)) {
			throw sessionExpired();
		}
		AdminUser admin = this.userRepository.findById(session.getAdminUserId())
			.orElseThrow(AdminUserServiceImpl::sessionExpired);
		if (!admin.isActive()) {
			revoke(session, now);
			throw sessionExpired();
		}
		String newRefreshToken = OpaqueTokenHelper.generate();
		session.rotate(OpaqueTokenHelper.hash(newRefreshToken), now, this.properties.idleTimeout());
		return issue(admin, session, newRefreshToken);
	}

	@Override
	@Transactional
	public void logout(String refreshToken) {
		Instant now = this.clock.instant();
		findSession(refreshToken).ifPresent((session) -> revoke(session, now));
	}

	@Override
	@Transactional(readOnly = true)
	public AdminResponseDTO getMe(UUID adminId) {
		return findById(adminId).orElseThrow(AdminUserServiceImpl::sessionExpired);
	}

	@Override
	@Transactional
	public void bootstrapFirstAdmin() {
		AdminProperties.Bootstrap bootstrap = this.properties.bootstrap();
		if (bootstrap == null || bootstrap.email() == null || bootstrap.email().isBlank()
				|| this.userRepository.count() > 0) {
			return;
		}
		if (bootstrap.password() == null || bootstrap.password().length() < AdminConstants.BOOTSTRAP_PASSWORD_MIN_LENGTH) {
			logger.warn("Skipping first admin: projecty.admin.bootstrap.password must be at least {} characters",
					AdminConstants.BOOTSTRAP_PASSWORD_MIN_LENGTH);
			return;
		}
		this.userRepository.save(new AdminUser(bootstrap.email().strip(), bootstrap.fullName(),
				this.passwordEncoder.encode(bootstrap.password()), this.clock.instant()));
		logger.info("Created the first platform admin: {}", bootstrap.email());
	}

	private AdminSessionTokensDTO issue(AdminUser admin, AdminSession session, String refreshToken) {
		AdminAccessTokenClaimsDTO claims = new AdminAccessTokenClaimsDTO(admin.getId(), admin.getEmail(),
				admin.getFullName(), session.getId());
		return new AdminSessionTokensDTO(this.tokenWriteService.issueAdminAccessToken(claims), refreshToken,
				session.expiresAt());
	}

	/**
	 * Ends the session and, with it, the access tokens already handed out for it.
	 */
	private void revoke(AdminSession session, Instant now) {
		session.revoke(now);
		this.tokenWriteService.denySession(session.getId());
	}

	private Optional<AdminSession> findSession(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			return Optional.empty();
		}
		return this.sessionRepository.findByRefreshTokenHash(OpaqueTokenHelper.hash(refreshToken));
	}

	private static String truncate(String userAgent) {
		return (userAgent != null && userAgent.length() > AdminConstants.USER_AGENT_MAX_LENGTH)
				? userAgent.substring(0, AdminConstants.USER_AGENT_MAX_LENGTH) : userAgent;
	}

	private static AppException invalidCredentials() {
		return new AppException(HttpStatus.UNAUTHORIZED, AdminConstants.ERROR_INVALID_CREDENTIALS,
				AdminConstants.MESSAGE_INVALID_CREDENTIALS);
	}

	private static AppException locked(AdminUser admin) {
		return new AppException(HttpStatus.LOCKED, AdminConstants.ERROR_ACCOUNT_LOCKED,
				AdminConstants.MESSAGE_ACCOUNT_LOCKED,
				Map.of(AdminConstants.DETAIL_LOCKED_UNTIL, admin.getLockedUntil()));
	}

	private static AppException sessionExpired() {
		return new AppException(HttpStatus.UNAUTHORIZED, AdminConstants.ERROR_SESSION_EXPIRED,
				AdminConstants.MESSAGE_SESSION_EXPIRED);
	}

}
