package com.projecty.platform.modules.admin.internal.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Admin console sessions are shorter than customer ones. {@code bootstrap} creates the first admin when none
 * exists; leave its email blank to skip.
 */
@ConfigurationProperties("projecty.admin")
public record AdminProperties(Duration idleTimeout, Duration absoluteTimeout, boolean refreshCookieSecure,
		int lockoutMaxAttempts, Duration lockoutWindow, Duration lockoutDuration, Bootstrap bootstrap) {

	public record Bootstrap(String email, String password, String fullName) {
	}

}
