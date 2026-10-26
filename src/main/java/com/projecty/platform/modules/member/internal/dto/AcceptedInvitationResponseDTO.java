package com.projecty.platform.modules.member.internal.dto;

import java.util.UUID;

/**
 * The portal just joined. The caller should refresh their access token so it lists the portal.
 */
public record AcceptedInvitationResponseDTO(UUID portalId, String portalSlug) {
}
