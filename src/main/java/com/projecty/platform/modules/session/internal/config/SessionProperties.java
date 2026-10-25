package com.projecty.platform.modules.session.internal.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SEC-BR-03 session limits.
 */
@ConfigurationProperties("projecty.session")
public record SessionProperties(Duration idleTimeout, Duration absoluteTimeout) {
}
