package com.projecty.platform.modules.impersonation.internal.dto;

/**
 * Same shape as the login response, so the customer app treats a support session like any session.
 */
public record SupportSessionTokenResponseDTO(String accessToken, String tokenType, long expiresIn) {
}
