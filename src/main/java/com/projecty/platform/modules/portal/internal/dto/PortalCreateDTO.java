package com.projecty.platform.modules.portal.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PortalCreateDTO(@NotBlank @Size(max = 100) String name, @NotBlank String slug) {
}
