package com.projecty.platform.modules.token.api.dto;

import java.util.UUID;

public record AdminAccessTokenClaimsDTO(UUID adminId, String email, String fullName, UUID sessionId) {
}
