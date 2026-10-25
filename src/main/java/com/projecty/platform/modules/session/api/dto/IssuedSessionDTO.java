package com.projecty.platform.modules.session.api.dto;

import java.time.Instant;

/**
 * Tokens for a new session: the access token for the response body, the refresh token for the
 * {@code py_refresh} cookie (see {@code common.security.helper.RefreshCookieHelper}).
 */
public record IssuedSessionDTO(String accessToken, long expiresInSeconds, String refreshToken,
		Instant refreshExpiresAt) {
}
