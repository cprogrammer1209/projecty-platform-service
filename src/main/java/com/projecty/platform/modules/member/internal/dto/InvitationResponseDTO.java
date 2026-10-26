package com.projecty.platform.modules.member.internal.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * A pending invitation in the portal's list.
 */
public record InvitationResponseDTO(UUID id, String email, UUID profileId, String profileName, String invitedByName,
		Instant createdAt, Instant expiresAt) {
}
