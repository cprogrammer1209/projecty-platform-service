package com.projecty.platform.modules.portal.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.projecty.platform.modules.portal.internal.entity.WeekStart;

public record PortalSettingsUpdateDTO(@NotBlank String timeZone, @NotBlank String dateFormat,
		@NotBlank String currency, @NotNull WeekStart weekStart) {
}
