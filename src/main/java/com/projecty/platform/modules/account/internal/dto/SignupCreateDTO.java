package com.projecty.platform.modules.account.internal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupCreateDTO(@NotBlank @Size(max = 200) String fullName,
		@NotBlank @Email @Size(max = 320) String email, @NotBlank String password) {
}
