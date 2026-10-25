package com.projecty.platform.modules.token.api.dto;

public record IssuedAccessTokenDTO(String token, long expiresInSeconds) {
}
