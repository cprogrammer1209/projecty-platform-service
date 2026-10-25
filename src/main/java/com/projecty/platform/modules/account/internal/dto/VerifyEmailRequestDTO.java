package com.projecty.platform.modules.account.internal.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequestDTO(@NotBlank String token) {
}
