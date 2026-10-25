package com.projecty.platform.common.security.helper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import com.projecty.platform.common.config.AppProperties;
import com.projecty.platform.common.constants.AppConstants;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * The customer refresh token cookie, shared by every feature that opens or ends a customer session (login,
 * support sessions). HttpOnly keeps it away from JavaScript, SameSite=Strict keeps other sites from sending
 * it, and the path limits it to the auth endpoints.
 */
@Component
public class RefreshCookieHelper {

	private final AppProperties properties;

	private final Clock clock;

	public RefreshCookieHelper(AppProperties properties, Clock clock) {
		this.properties = properties;
		this.clock = clock;
	}

	public ResponseCookie create(String refreshToken, Instant expiresAt) {
		return base(refreshToken).maxAge(Duration.between(this.clock.instant(), expiresAt)).build();
	}

	public ResponseCookie clear() {
		return base("").maxAge(Duration.ZERO).build();
	}

	private ResponseCookie.ResponseCookieBuilder base(String value) {
		return ResponseCookie.from(AppConstants.REFRESH_COOKIE_NAME, value)
			.httpOnly(true)
			.secure(this.properties.refreshCookie().secure())
			.sameSite(AppConstants.REFRESH_COOKIE_SAME_SITE)
			.path(AppConstants.REFRESH_COOKIE_PATH);
	}

}
