package com.projecty.platform.modules.account.internal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailRequestDTO(@NotBlank @Email String email) {
}
