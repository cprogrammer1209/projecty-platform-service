package com.projecty.platform.modules.session.internal.helper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import com.projecty.platform.modules.session.internal.config.SessionProperties;
import com.projecty.platform.modules.session.internal.constants.SessionConstants;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * The refresh token cookie. HttpOnly keeps it away from JavaScript, SameSite=Strict keeps other sites from
 * sending it, and the path limits it to the auth endpoints.
 */
@Component
public class RefreshCookieHelper {

	private final SessionProperties properties;

	private final Clock clock;

	public RefreshCookieHelper(SessionProperties properties, Clock clock) {
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
		return ResponseCookie.from(SessionConstants.REFRESH_COOKIE_NAME, value)
			.httpOnly(true)
			.secure(this.properties.refreshCookieSecure())
			.sameSite(SessionConstants.REFRESH_COOKIE_SAME_SITE)
			.path(SessionConstants.REFRESH_COOKIE_PATH);
	}

}
