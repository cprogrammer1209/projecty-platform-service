package com.projecty.platform.modules.admin.internal.dto;

import java.time.Instant;

import com.projecty.platform.modules.token.api.dto.IssuedAccessTokenDTO;

/**
 * Access token for the body, refresh token for the HttpOnly cookie.
 */
public record AdminSessionTokensDTO(IssuedAccessTokenDTO accessToken, String refreshToken,
		Instant refreshExpiresAt) {
}
