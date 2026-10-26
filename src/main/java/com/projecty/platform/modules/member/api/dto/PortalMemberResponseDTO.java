package com.projecty.platform.modules.member.api.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * @param status {@code ACTIVE} or {@code DEACTIVATED}
 */
public record PortalMemberResponseDTO(UUID userId, String profile, String profileName, String status,
		Instant joinedAt) {
}
