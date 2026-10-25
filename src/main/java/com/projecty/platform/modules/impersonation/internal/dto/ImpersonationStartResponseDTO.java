package com.projecty.platform.modules.impersonation.internal.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * @param launchUrl customer app link that opens the support session; works once, until {@code launchExpiresAt}
 */
public record ImpersonationStartResponseDTO(UUID id, String launchUrl, Instant launchExpiresAt,
		Instant expiresAt) {
}
