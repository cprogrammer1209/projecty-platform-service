package com.projecty.platform.modules.token.internal.config;

import java.util.List;

import com.projecty.platform.modules.token.internal.service.SigningKeyService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Signs tokens with this service's keys and checks incoming tokens against the same keys, including issuer
 * and audience. Other services check tokens with the published keys at {@code /.well-known/jwks.json}.
 */
@Configuration
public class JwtConfig {

	@Bean
	public JwtEncoder jwtEncoder(SigningKeyService signingKeyService) {
		return new NimbusJwtEncoder(signingKeyService.jwkSource());
	}

	@Bean
	public JwtDecoder jwtDecoder(SigningKeyService signingKeyService, TokenProperties properties) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSource(signingKeyService.jwkSource()).build();
		OAuth2TokenValidator<Jwt> audience = new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
				(aud) -> aud != null && aud.contains(properties.audience()));
		decoder.setJwtValidator(JwtValidators
			.createDefaultWithValidators(JwtValidators.createDefaultWithIssuer(properties.issuer()), audience));
		return decoder;
	}

}
