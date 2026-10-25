package com.projecty.platform.modules.session.internal.dto;

public record AccessTokenResponseDTO(String accessToken, String tokenType, long expiresIn) {
}
