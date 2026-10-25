package com.projecty.platform.modules.admin.internal.dto;

public record AdminAccessTokenResponseDTO(String accessToken, String tokenType, long expiresIn) {
}
