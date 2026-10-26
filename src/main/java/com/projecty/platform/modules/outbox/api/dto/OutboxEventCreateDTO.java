package com.projecty.platform.modules.outbox.api.dto;

import java.util.Map;
import java.util.UUID;

/**
 * One business event. {@code eventType} and {@code aggregateType} take the values in
 * {@code modules.shared.constants.OutboxEventConstants}. Events of one aggregate are published in order.
 * @param portalId the tenant the event concerns, or null for platform-level events
 * @param payload ids and before/after values only; it is stored as JSON
 */
public record OutboxEventCreateDTO(UUID portalId, String aggregateType, UUID aggregateId, String eventType,
		Map<String, Object> payload) {
}
