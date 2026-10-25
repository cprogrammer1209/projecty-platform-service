package com.projecty.platform.modules.audit.internal.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditEventResponseDTO(UUID id, Instant occurredAt, String category, String action, String actorType,
		UUID actorId, String actorName, UUID portalId, UUID targetUserId, String targetName, String detail) {
}
