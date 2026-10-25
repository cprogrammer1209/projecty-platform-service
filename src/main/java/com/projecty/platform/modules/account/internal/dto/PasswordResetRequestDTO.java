package com.projecty.platform.modules.account.internal.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequestDTO(@NotBlank String token, @NotBlank String newPassword) {
}
