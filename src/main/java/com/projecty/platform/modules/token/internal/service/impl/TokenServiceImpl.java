package com.projecty.platform.modules.token.internal.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.projecty.platform.common.constants.AppConstants;

import com.projecty.platform.modules.token.api.dto.AccessTokenClaimsDTO;
import com.projecty.platform.modules.token.api.dto.AdminAccessTokenClaimsDTO;
import com.projecty.platform.modules.token.api.dto.ImpersonationClaimDTO;
import com.projecty.platform.modules.token.api.dto.IssuedAccessTokenDTO;
import com.projecty.platform.modules.token.internal.config.TokenProperties;
import com.projecty.platform.modules.token.internal.constants.TokenConstants;
import com.projecty.platform.modules.token.internal.service.SigningKeyService;
import com.projecty.platform.modules.token.internal.service.TokenService;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Builds and signs access tokens. The claims are what every service relies on: who the user is ({@code sub})
 * and which portals they may act in ({@code portals}). A support session adds who is really acting
 * ({@code act}). Admin console tokens use a separate audience and carry {@code role}.
 */
@Service
public class TokenServiceImpl implements TokenService {

	private final JwtEncoder encoder;

	private final SigningKeyService signingKeyService;

	private final TokenProperties properties;

	private final Clock clock;

	public TokenServiceImpl(JwtEncoder encoder, SigningKeyService signingKeyService, TokenProperties properties,
			Clock clock) {
		this.encoder = encoder;
		this.signingKeyService = signingKeyService;
		this.properties = properties;
		this.clock = clock;
	}

	@Override
	public IssuedAccessTokenDTO issueAccessToken(AccessTokenClaimsDTO request) {
		JwtClaimsSet.Builder claims = base(this.properties.audience(), request.userId(), request.sessionId())
			.claim(TokenConstants.CLAIM_EMAIL, request.email())
			.claim(AppConstants.CLAIM_NAME, request.fullName())
			.claim(AppConstants.CLAIM_PORTALS, request.portalIds().stream().map(UUID::toString).toList());
		ImpersonationClaimDTO impersonation = request.impersonation();
		if (impersonation != null) {
			claims.claim(TokenConstants.CLAIM_ACTOR, actor(impersonation))
				.claim(TokenConstants.CLAIM_IMPERSONATION,
						Map.of(TokenConstants.CLAIM_ID, impersonation.impersonationId().toString(),
								TokenConstants.CLAIM_EXPIRES, impersonation.expiresAt().getEpochSecond()));
		}
		return sign(claims.build());
	}

	@Override
	public IssuedAccessTokenDTO issueAdminAccessToken(AdminAccessTokenClaimsDTO request) {
		return sign(base(this.properties.adminAudience(), request.adminId(), request.sessionId())
			.claim(TokenConstants.CLAIM_EMAIL, request.email())
			.claim(AppConstants.CLAIM_NAME, request.fullName())
			.claim(TokenConstants.CLAIM_ROLE, AppConstants.ROLE_PLATFORM_ADMIN)
			.build());
	}

	private JwtClaimsSet.Builder base(String audience, UUID subject, UUID sessionId) {
		Instant now = this.clock.instant();
		return JwtClaimsSet.builder()
			.issuer(this.properties.issuer())
			.audience(List.of(audience))
			.subject(subject.toString())
			.issuedAt(now)
			.expiresAt(now.plus(this.properties.accessTokenTtl()))
			.claim(TokenConstants.CLAIM_SESSION_ID, sessionId.toString());
	}

	private IssuedAccessTokenDTO sign(JwtClaimsSet claims) {
		JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
			.keyId(this.signingKeyService.signingKey().getKeyID())
			.build();
		String token = this.encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new IssuedAccessTokenDTO(token, this.properties.accessTokenTtl().toSeconds());
	}

	private static Map<String, Object> actor(ImpersonationClaimDTO impersonation) {
		Map<String, Object> actor = new LinkedHashMap<>();
		actor.put(JwtClaimNames.SUB, impersonation.adminId().toString());
		actor.put(AppConstants.CLAIM_NAME, impersonation.adminName());
		actor.put(TokenConstants.CLAIM_ACTOR_TYPE, TokenConstants.ACTOR_TYPE_ADMIN);
		return actor;
	}

}
