package com.projecty.platform.modules.session.internal.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.common.exception.AppException;
import com.projecty.platform.modules.account.api.UserLookUpService;
import com.projecty.platform.modules.account.api.UserWriteService;
import com.projecty.platform.modules.account.api.dto.UserResponseDTO;
import com.projecty.platform.modules.member.api.MemberLookUpService;
import com.projecty.platform.modules.session.api.dto.IssuedSessionDTO;
import com.projecty.platform.modules.session.api.dto.SupportSessionCreateDTO;
import com.projecty.platform.modules.session.api.event.SupportSessionExpiredEvent;
import com.projecty.platform.modules.session.internal.config.SessionProperties;
import com.projecty.platform.modules.session.internal.constants.SessionConstants;
import com.projecty.platform.modules.session.internal.dto.SessionTokensDTO;
import com.projecty.platform.modules.session.internal.entity.AuthSession;
import com.projecty.platform.modules.session.internal.entity.RefreshToken;
import com.projecty.platform.modules.session.internal.mapper.SessionMapper;
import com.projecty.platform.modules.session.internal.repository.AuthSessionRepository;
import com.projecty.platform.modules.session.internal.repository.RefreshTokenRepository;
import com.projecty.platform.modules.session.internal.service.SessionService;
import com.projecty.platform.modules.shared.helper.OpaqueTokenHelper;
import com.projecty.platform.modules.token.api.TokenWriteService;
import com.projecty.platform.modules.token.api.dto.AccessTokenClaimsDTO;
import com.projecty.platform.modules.token.api.dto.ImpersonationClaimDTO;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Composite service: owns sessions and refresh tokens; uses the account module to check credentials, the
 * portal module for the token's portal list, and the token module to sign access tokens. Support sessions are
 * opened for the impersonation module through {@code SessionWriteService}; this module doesn't depend on it.
 */
@Service
public class SessionServiceImpl implements SessionService {

	private final AuthSessionRepository sessionRepository;

	private final RefreshTokenRepository refreshTokenRepository;

	private final UserLookUpService userLookUpService;

	private final UserWriteService userWriteService;

	private final MemberLookUpService memberLookUpService;

	private final TokenWriteService tokenWriteService;

	private final SessionMapper sessionMapper;

	private final ApplicationEventPublisher events;

	private final SessionProperties properties;

	private final Clock clock;

	public SessionServiceImpl(AuthSessionRepository sessionRepository,
			RefreshTokenRepository refreshTokenRepository, UserLookUpService userLookUpService,
			UserWriteService userWriteService, MemberLookUpService memberLookUpService,
			TokenWriteService tokenWriteService, SessionMapper sessionMapper, ApplicationEventPublisher events,
			SessionProperties properties, Clock clock) {
		this.sessionRepository = sessionRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.userLookUpService = userLookUpService;
		this.userWriteService = userWriteService;
		this.memberLookUpService = memberLookUpService;
		this.tokenWriteService = tokenWriteService;
		this.sessionMapper = sessionMapper;
		this.events = events;
		this.properties = properties;
		this.clock = clock;
	}

	/**
	 * Not rolled back on failure, so failed login attempts are still counted.
	 */
	@Override
	@Transactional(noRollbackFor = AppException.class)
	public SessionTokensDTO login(String email, String password, String userAgent, String ip) {
		UserResponseDTO user = this.userWriteService.authenticate(email, password);
		Instant now = this.clock.instant();
		AuthSession session = this.sessionRepository.save(new AuthSession(user.id(), truncate(userAgent), ip, now,
				this.properties.idleTimeout(), this.properties.absoluteTimeout()));
		return issue(user, session, now);
	}

	/**
	 * Not rolled back on failure, so a revocation caused by token reuse (or a support session reaching its end)
	 * is kept.
	 */
	@Override
	@Transactional(noRollbackFor = AppException.class)
	public SessionTokensDTO refresh(String refreshToken) {
		Instant now = this.clock.instant();
		RefreshToken token = findToken(refreshToken).orElseThrow(SessionServiceImpl::sessionExpired);
		AuthSession session = this.sessionRepository.findById(token.getSessionId())
			.orElseThrow(SessionServiceImpl::sessionExpired);
		if (token.isUsed()) {
			revoke(session, now);
			throw sessionExpired();
		}
		if (session.isImpersonationOver(now)) {
			revoke(session, now);
			this.events.publishEvent(new SupportSessionExpiredEvent(session.getImpersonationId()));
			throw sessionExpired();
		}
		if (!session.isUsable(now)) {
			throw sessionExpired();
		}
		UserResponseDTO user = this.userLookUpService.findById(session.getUserId())
			.orElseThrow(SessionServiceImpl::sessionExpired);
		if (!user.active()) {
			revoke(session, now);
			throw sessionExpired();
		}
		token.markUsed(now);
		session.touch(now, this.properties.idleTimeout());
		return issue(user, session, now);
	}

	@Override
	@Transactional
	public void logout(String refreshToken) {
		Instant now = this.clock.instant();
		findToken(refreshToken).flatMap((token) -> this.sessionRepository.findById(token.getSessionId()))
			.ifPresent((session) -> revoke(session, now));
	}

	@Override
	@Transactional
	public void revokeAllForUser(UUID userId) {
		// The bulk update below cannot say which sessions it ended, so deny them first.
		this.sessionRepository.findActiveIdsByUserId(userId).forEach(this.tokenWriteService::denySession);
		this.sessionRepository.revokeAllForUser(userId, this.clock.instant());
	}

	@Override
	@Transactional
	public IssuedSessionDTO openSupportSession(SupportSessionCreateDTO request) {
		UserResponseDTO user = this.userLookUpService.findById(request.userId())
			.filter(UserResponseDTO::active)
			.orElseThrow(SessionServiceImpl::sessionExpired);
		Instant now = this.clock.instant();
		AuthSession session = this.sessionRepository.save(AuthSession.impersonated(user.id(),
				request.impersonationId(), request.adminId(), request.adminName(), request.expiresAt(),
				truncate(request.userAgent()), request.ip(), now, this.properties.idleTimeout()));
		return this.sessionMapper.toIssued(issue(user, session, now));
	}

	@Override
	@Transactional
	public Optional<UUID> endSupportSession(String refreshToken) {
		Instant now = this.clock.instant();
		return findToken(refreshToken).flatMap((token) -> this.sessionRepository.findById(token.getSessionId()))
			.filter(AuthSession::isImpersonated)
			.map((session) -> {
				revoke(session, now);
				return session.getImpersonationId();
			});
	}

	/**
	 * A support session's tokens also name the acting admin, read from the session itself.
	 */
	private SessionTokensDTO issue(UserResponseDTO user, AuthSession session, Instant now) {
		String refreshToken = OpaqueTokenHelper.generate();
		this.refreshTokenRepository.save(new RefreshToken(session.getId(), OpaqueTokenHelper.hash(refreshToken), now));
		ImpersonationClaimDTO actor = session.isImpersonated()
				? new ImpersonationClaimDTO(session.getImpersonationId(), session.getImpersonatorAdminId(),
						session.getImpersonatorName(), session.getAbsoluteExpiresAt())
				: null;
		AccessTokenClaimsDTO claims = new AccessTokenClaimsDTO(user.id(), user.email(), user.fullName(),
				this.memberLookUpService.findActivePortalIds(user.id()), session.getId(), actor);
		return new SessionTokensDTO(this.tokenWriteService.issueAccessToken(claims), refreshToken,
				session.expiresAt());
	}

	/**
	 * Ends the session and, with it, the access tokens already handed out for it.
	 */
	private void revoke(AuthSession session, Instant now) {
		session.revoke(now);
		this.tokenWriteService.denySession(session.getId());
	}

	private Optional<RefreshToken> findToken(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			return Optional.empty();
		}
		return this.refreshTokenRepository.findByTokenHash(OpaqueTokenHelper.hash(refreshToken));
	}

	private static String truncate(String userAgent) {
		return (userAgent != null && userAgent.length() > SessionConstants.USER_AGENT_MAX_LENGTH)
				? userAgent.substring(0, SessionConstants.USER_AGENT_MAX_LENGTH) : userAgent;
	}

	private static AppException sessionExpired() {
		return new AppException(HttpStatus.UNAUTHORIZED, SessionConstants.ERROR_SESSION_EXPIRED,
				SessionConstants.MESSAGE_SESSION_EXPIRED);
	}

}
