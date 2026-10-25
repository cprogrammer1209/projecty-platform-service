package com.projecty.platform.modules.token.internal.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.projecty.platform.common.constants.AppConstants;
import com.projecty.platform.modules.token.internal.service.SigningKeyService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

/**
 * Signs tokens with this service's keys and checks incoming tokens against the same keys, including issuer
 * and audience. Other services check tokens with the published keys at {@code /.well-known/jwks.json}.
 *
 * Two audiences are accepted: customer tokens get role USER and admin console tokens role PLATFORM_ADMIN;
 * {@code SecurityConfig} decides which role each endpoint needs.
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
				(aud) -> aud != null
						&& (aud.contains(properties.audience()) || aud.contains(properties.adminAudience())));
		decoder.setJwtValidator(JwtValidators
			.createDefaultWithValidators(JwtValidators.createDefaultWithIssuer(properties.issuer()), audience));
		return decoder;
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter(TokenProperties properties) {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter((jwt) -> roles(jwt, properties));
		return converter;
	}

	private static Collection<GrantedAuthority> roles(Jwt jwt, TokenProperties properties) {
		List<String> audience = jwt.getAudience();
		List<GrantedAuthority> roles = new ArrayList<>();
		if (audience.contains(properties.audience())) {
			roles.add(new SimpleGrantedAuthority(AppConstants.ROLE_PREFIX + AppConstants.ROLE_USER));
		}
		if (audience.contains(properties.adminAudience())) {
			roles.add(new SimpleGrantedAuthority(AppConstants.ROLE_PREFIX + AppConstants.ROLE_PLATFORM_ADMIN));
		}
		return roles;
	}

}
