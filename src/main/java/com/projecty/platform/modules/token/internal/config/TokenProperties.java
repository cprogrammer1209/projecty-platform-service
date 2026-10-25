package com.projecty.platform.modules.token.internal.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("projecty.token")
public record TokenProperties(String issuer, String audience, Duration accessTokenTtl) {
}
