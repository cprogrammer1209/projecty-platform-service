package com.projecty.platform.modules.admin.internal.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequestDTO(@NotBlank String email, @NotBlank String password) {
}
