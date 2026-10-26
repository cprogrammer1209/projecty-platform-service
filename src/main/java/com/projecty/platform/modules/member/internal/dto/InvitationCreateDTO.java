package com.projecty.platform.modules.member.internal.dto;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InvitationCreateDTO(@NotBlank @Email @Size(max = 320) String email, @NotNull UUID profileId) {
}
