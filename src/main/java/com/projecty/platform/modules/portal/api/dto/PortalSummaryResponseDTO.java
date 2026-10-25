package com.projecty.platform.modules.portal.api.dto;

import java.time.Instant;
import java.util.UUID;

public record PortalSummaryResponseDTO(UUID id, String name, String slug, UUID ownerUserId, Instant createdAt,
		long memberCount, long activeMemberCount) {
}
