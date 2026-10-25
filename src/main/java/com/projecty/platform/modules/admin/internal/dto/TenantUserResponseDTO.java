package com.projecty.platform.modules.admin.internal.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * @param active the membership and the account are both active, so the user can be impersonated
 */
public record TenantUserResponseDTO(UUID userId, String fullName, String email, String profile, boolean active,
		Instant lastLoginAt, Instant joinedAt) {
}
