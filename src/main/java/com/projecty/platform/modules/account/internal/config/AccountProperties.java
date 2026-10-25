package com.projecty.platform.modules.account.internal.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("projecty.account")
public record AccountProperties(Duration verifyEmailTokenTtl, Duration resetPasswordTokenTtl,
		int lockoutMaxAttempts, Duration lockoutWindow, Duration lockoutDuration) {
}
