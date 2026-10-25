package com.projecty.platform.modules.portal.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @param timeZone the creator's time zone (optional), used as the portal's default
 */
public record PortalCreateDTO(@NotBlank @Size(max = 100) String name, @NotBlank String slug, String timeZone) {
}
