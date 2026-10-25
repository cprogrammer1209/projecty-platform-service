package com.projecty.platform.modules.portal.internal.dto;

import java.util.UUID;

/**
 * The portal of the current request, with the caller's profile in it.
 * @param canManageSettings whether the caller may change the settings (Owner, until profiles arrive in Slice 4)
 */
public record CurrentPortalResponseDTO(UUID id, String name, String slug, String profile, String timeZone,
		String dateFormat, String currency, String weekStart, boolean canManageSettings) {
}
