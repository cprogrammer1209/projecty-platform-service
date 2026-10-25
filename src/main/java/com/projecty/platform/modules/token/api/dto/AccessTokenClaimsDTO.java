package com.projecty.platform.modules.token.api.dto;

import java.util.List;
import java.util.UUID;

/**
 * @param impersonation set only for a support session opened by a platform admin, otherwise null
 */
public record AccessTokenClaimsDTO(UUID userId, String email, String fullName, List<UUID> portalIds,
		UUID sessionId, ImpersonationClaimDTO impersonation) {
}
