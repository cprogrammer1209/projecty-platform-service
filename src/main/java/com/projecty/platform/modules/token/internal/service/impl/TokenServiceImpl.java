package com.projecty.platform.modules.token.internal.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.projecty.platform.modules.token.api.dto.AccessTokenClaimsDTO;
import com.projecty.platform.modules.token.api.dto.IssuedAccessTokenDTO;
import com.projecty.platform.modules.token.internal.config.TokenProperties;
import com.projecty.platform.modules.token.internal.constants.TokenConstants;
import com.projecty.platform.modules.token.internal.service.SigningKeyService;
import com.projecty.platform.modules.token.internal.service.TokenService;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Builds and signs access tokens. The claims are what every service relies on: who the user is ({@code sub})
 * and which portals they may act in ({@code portals}).
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
		Instant now = this.clock.instant();
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(this.properties.issuer())
			.audience(List.of(this.properties.audience()))
			.subject(request.userId().toString())
			.issuedAt(now)
			.expiresAt(now.plus(this.properties.accessTokenTtl()))
			.claim(TokenConstants.CLAIM_EMAIL, request.email())
			.claim(TokenConstants.CLAIM_NAME, request.fullName())
			.claim(TokenConstants.CLAIM_PORTALS, request.portalIds().stream().map(UUID::toString).toList())
			.claim(TokenConstants.CLAIM_SESSION_ID, request.sessionId().toString())
			.build();
		JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
			.keyId(this.signingKeyService.signingKey().getKeyID())
			.build();
		String token = this.encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new IssuedAccessTokenDTO(token, this.properties.accessTokenTtl().toSeconds());
	}

}
