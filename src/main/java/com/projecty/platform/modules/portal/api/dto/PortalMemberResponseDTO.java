package com.projecty.platform.modules.portal.api.dto;

import java.time.Instant;
import java.util.UUID;

public record PortalMemberResponseDTO(UUID userId, String profile, String status, Instant joinedAt) {
}
