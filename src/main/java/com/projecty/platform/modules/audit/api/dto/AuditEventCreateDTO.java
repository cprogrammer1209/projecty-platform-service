package com.projecty.platform.modules.audit.api.dto;

import java.util.UUID;

/**
 * One audited action. {@code category}, {@code action} and {@code actorType} take the values in
 * {@code modules.shared.constants.AuditEventConstants}.
 * @param portalId the tenant the action concerns, or null for platform-level actions
 */
public record AuditEventCreateDTO(String category, String action, String actorType, UUID actorId,
		String actorName, UUID portalId, UUID targetUserId, String targetName, String detail) {
}
