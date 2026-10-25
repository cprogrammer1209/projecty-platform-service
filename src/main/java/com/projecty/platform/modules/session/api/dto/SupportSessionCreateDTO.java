package com.projecty.platform.modules.session.api.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * A session for {@code userId} opened by platform admin {@code adminId} (support impersonation). It ends at
 * {@code expiresAt} at the latest; its access tokens name the admin as the actor.
 */
public record SupportSessionCreateDTO(UUID userId, UUID impersonationId, UUID adminId, String adminName,
		Instant expiresAt, String userAgent, String ip) {
}
