package com.projecty.platform.modules.token.api.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Who is really acting when a platform admin signs in as a customer user.
 */
public record ImpersonationClaimDTO(UUID impersonationId, UUID adminId, String adminName, Instant expiresAt) {
}
