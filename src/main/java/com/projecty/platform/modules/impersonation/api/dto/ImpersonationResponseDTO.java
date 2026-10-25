package com.projecty.platform.modules.impersonation.api.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * A support session: platform admin {@code adminId} acting as {@code targetUserId} in {@code portalId} until
 * {@code expiresAt}, unless ended earlier ({@code endedAt}).
 */
public record ImpersonationResponseDTO(UUID id, UUID adminId, String adminName, UUID portalId, UUID targetUserId,
		String targetName, Instant expiresAt, Instant endedAt) {
}
