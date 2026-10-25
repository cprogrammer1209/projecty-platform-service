package com.projecty.platform.modules.impersonation.internal.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code codeTtl}: how long the launch link works after the admin submits the request.
 */
@ConfigurationProperties("projecty.impersonation")
public record ImpersonationProperties(Duration codeTtl) {
}
