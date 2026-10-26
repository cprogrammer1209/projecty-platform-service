package com.projecty.platform.common.security.config;

import com.projecty.platform.common.config.AppProperties;
import com.projecty.platform.common.constants.AppConstants;
import com.projecty.platform.common.security.filter.PortalContextFilter;
import com.projecty.platform.common.security.handler.RestAccessDeniedHandler;
import com.projecty.platform.common.security.handler.RestAuthenticationEntryPoint;
import com.projecty.platform.common.security.handler.SecurityErrorWriter;
import com.projecty.platform.common.service.PortalAccessService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Stateless API security. Access tokens are JWTs checked by Spring's OAuth2 resource server support; the
 * {@code JwtDecoder} bean comes from the token module.
 *
 * CSRF protection is off: API calls authenticate with a bearer header, which browsers never attach on their
 * own. The refresh cookies are HttpOnly, SameSite=Strict and scoped to /api/auth and /api/admin/auth.
 *
 * Customer tokens carry role USER and admin console tokens role PLATFORM_ADMIN (mapped from the audience by
 * the token module), so neither kind of token works on the other's endpoints. {@code PortalContextFilter}
 * then checks the active portal of customer requests ({@code X-Portal-Id}).
 */
@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain apiSecurity(HttpSecurity http, RestAuthenticationEntryPoint authenticationEntryPoint,
			RestAccessDeniedHandler accessDeniedHandler, SecurityErrorWriter errorWriter,
			PortalAccessService portalAccessService) throws Exception {
		return http.csrf((csrf) -> csrf.disable())
			.cors(Customizer.withDefaults())
			.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests((auth) -> auth
				.requestMatchers("/api/auth/**", "/api/admin/auth/login", "/api/admin/auth/refresh",
						"/api/admin/auth/logout")
				.permitAll()
				.requestMatchers(HttpMethod.GET, "/.well-known/jwks.json", "/actuator/health/**", "/v3/api-docs/**",
						"/swagger-ui/**", "/swagger-ui.html")
				.permitAll()
				.requestMatchers("/api/admin/**")
				.hasRole(AppConstants.ROLE_PLATFORM_ADMIN)
				.requestMatchers("/api/**")
				.hasRole(AppConstants.ROLE_USER)
				.anyRequest()
				.authenticated())
			.oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults())
				.authenticationEntryPoint(authenticationEntryPoint)
				.accessDeniedHandler(accessDeniedHandler))
			.exceptionHandling((exceptions) -> exceptions.authenticationEntryPoint(authenticationEntryPoint)
				.accessDeniedHandler(accessDeniedHandler))
			.addFilterAfter(new PortalContextFilter(errorWriter, portalAccessService), BearerTokenAuthenticationFilter.class)
			.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource(AppProperties properties) {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(properties.cors().allowedOrigins());
		config.addAllowedMethod("*");
		config.addAllowedHeader("*");
		config.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", config);
		return source;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

}
