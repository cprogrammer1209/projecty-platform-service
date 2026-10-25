package com.projecty.platform.modules.admin.internal.helper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import com.projecty.platform.modules.admin.internal.config.AdminProperties;
import com.projecty.platform.modules.admin.internal.constants.AdminConstants;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * The admin refresh token cookie: HttpOnly, SameSite=Strict, sent only to /api/admin/auth.
 */
@Component
public class AdminRefreshCookieHelper {

	private final AdminProperties properties;

	private final Clock clock;

	public AdminRefreshCookieHelper(AdminProperties properties, Clock clock) {
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
		return ResponseCookie.from(AdminConstants.REFRESH_COOKIE_NAME, value)
			.httpOnly(true)
			.secure(this.properties.refreshCookieSecure())
			.sameSite(AdminConstants.REFRESH_COOKIE_SAME_SITE)
			.path(AdminConstants.REFRESH_COOKIE_PATH);
	}

}
