package com.projecty.platform.modules.session.internal.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.common.exception.AppException;
import com.projecty.platform.modules.account.api.UserLookUpService;
import com.projecty.platform.modules.account.api.UserWriteService;
import com.projecty.platform.modules.account.api.dto.UserResponseDTO;
import com.projecty.platform.modules.portal.api.PortalLookUpService;
import com.projecty.platform.modules.session.internal.config.SessionProperties;
import com.projecty.platform.modules.session.internal.constants.SessionConstants;
import com.projecty.platform.modules.session.internal.dto.SessionTokensDTO;
import com.projecty.platform.modules.session.internal.entity.AuthSession;
import com.projecty.platform.modules.session.internal.entity.RefreshToken;
import com.projecty.platform.modules.session.internal.repository.AuthSessionRepository;
import com.projecty.platform.modules.session.internal.repository.RefreshTokenRepository;
import com.projecty.platform.modules.session.internal.service.SessionService;
import com.projecty.platform.modules.shared.helper.OpaqueTokenHelper;
import com.projecty.platform.modules.token.api.TokenWriteService;
import com.projecty.platform.modules.token.api.dto.AccessTokenClaimsDTO;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Composite service: owns sessions and refresh tokens; uses the account module to check credentials, the
 * portal module for the token's portal list, and the token module to sign access tokens.
 */
@Service
public class SessionServiceImpl implements SessionService {

	private final AuthSessionRepository sessionRepository;

	private final RefreshTokenRepository refreshTokenRepository;

	private final UserLookUpService userLookUpService;

	private final UserWriteService userWriteService;

	private final PortalLookUpService portalLookUpService;

	private final TokenWriteService tokenWriteService;

	private final SessionProperties properties;

	private final Clock clock;

	public SessionServiceImpl(AuthSessionRepository sessionRepository,
			RefreshTokenRepository refreshTokenRepository, UserLookUpService userLookUpService,
			UserWriteService userWriteService, PortalLookUpService portalLookUpService,
			TokenWriteService tokenWriteService, SessionProperties properties, Clock clock) {
		this.sessionRepository = sessionRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.userLookUpService = userLookUpService;
		this.userWriteService = userWriteService;
		this.portalLookUpService = portalLookUpService;
		this.tokenWriteService = tokenWriteService;
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
	 * Not rolled back on failure, so a revocation caused by token reuse is kept.
	 */
	@Override
	@Transactional(noRollbackFor = AppException.class)
	public SessionTokensDTO refresh(String refreshToken) {
		Instant now = this.clock.instant();
		RefreshToken token = findToken(refreshToken).orElseThrow(SessionServiceImpl::sessionExpired);
		AuthSession session = this.sessionRepository.findById(token.getSessionId())
			.orElseThrow(SessionServiceImpl::sessionExpired);
		if (token.isUsed()) {
			session.revoke(now);
			throw sessionExpired();
		}
		if (!session.isUsable(now)) {
			throw sessionExpired();
		}
		UserResponseDTO user = this.userLookUpService.findById(session.getUserId())
			.orElseThrow(SessionServiceImpl::sessionExpired);
		if (!user.active()) {
			session.revoke(now);
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
			.ifPresent((session) -> session.revoke(now));
	}

	@Override
	@Transactional
	public void revokeAllForUser(UUID userId) {
		this.sessionRepository.revokeAllForUser(userId, this.clock.instant());
	}

	private SessionTokensDTO issue(UserResponseDTO user, AuthSession session, Instant now) {
		String refreshToken = OpaqueTokenHelper.generate();
		this.refreshTokenRepository.save(new RefreshToken(session.getId(), OpaqueTokenHelper.hash(refreshToken), now));
		AccessTokenClaimsDTO claims = new AccessTokenClaimsDTO(user.id(), user.email(), user.fullName(),
				this.portalLookUpService.findActivePortalIds(user.id()), session.getId());
		return new SessionTokensDTO(this.tokenWriteService.issueAccessToken(claims), refreshToken,
				session.expiresAt());
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
