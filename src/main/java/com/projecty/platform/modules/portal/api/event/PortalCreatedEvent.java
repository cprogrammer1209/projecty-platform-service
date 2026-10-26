package com.projecty.platform.modules.portal.api.event;

import java.util.UUID;

/**
 * Published inside the creating transaction. Listeners run in the same transaction (the member module seeds
 * the portal's profiles and adds the owner as Admin); if one fails, the portal isn't created.
 */
public record PortalCreatedEvent(UUID portalId, UUID ownerUserId) {
}
