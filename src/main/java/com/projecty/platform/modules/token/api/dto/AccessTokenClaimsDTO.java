package com.projecty.platform.modules.token.api.dto;

import java.util.List;
import java.util.UUID;

public record AccessTokenClaimsDTO(UUID userId, String email, String fullName, List<UUID> portalIds,
		UUID sessionId) {
}
