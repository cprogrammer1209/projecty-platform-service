package com.projecty.platform.modules.session.internal.dto;

import java.time.Instant;

import com.projecty.platform.modules.token.api.dto.IssuedAccessTokenDTO;

/**
 * The result of a login or refresh: the access token for the response body, and the refresh token for the
 * HttpOnly cookie.
 */
public record SessionTokensDTO(IssuedAccessTokenDTO accessToken, String refreshToken, Instant refreshExpiresAt) {
}
