package com.projecty.platform.modules.member.internal.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * A row of the portal's user list (USR-US-03).
 * @param owner the portal owner can't be demoted or deactivated (PTL-BR-03)
 */
public record MemberResponseDTO(UUID userId, String fullName, String email, UUID profileId, String profile,
		String profileName, String status, boolean owner, Instant lastLoginAt, Instant joinedAt) {
}
