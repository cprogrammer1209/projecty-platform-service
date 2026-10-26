package com.projecty.platform.modules.portal.internal.dto;

import java.util.UUID;

/**
 * The portal of the current request and its settings. The caller's profile and permissions come from
 * {@code GET /api/portal/me} (member module).
 */
public record CurrentPortalResponseDTO(UUID id, String name, String slug, UUID ownerUserId, String timeZone,
		String dateFormat, String currency, String weekStart) {
}
