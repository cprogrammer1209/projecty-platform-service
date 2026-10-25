package com.projecty.platform.modules.token.internal.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code audience} is for customer tokens, {@code adminAudience} for admin console tokens.
 */
@ConfigurationProperties("projecty.token")
public record TokenProperties(String issuer, String audience, String adminAudience, Duration accessTokenTtl) {
}
