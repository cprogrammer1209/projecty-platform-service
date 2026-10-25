package com.projecty.platform.modules.admin.internal.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * A tenant (portal) as the admin console shows it.
 * @param lastActivityAt the most recent sign-in of any member, or null
 */
public record TenantResponseDTO(UUID id, String name, String slug, UUID ownerUserId, String ownerName,
		String ownerEmail, Instant createdAt, long userCount, long activeUserCount, Instant lastActivityAt) {
}
