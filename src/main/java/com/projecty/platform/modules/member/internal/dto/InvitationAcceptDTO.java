package com.projecty.platform.modules.member.internal.dto;

import jakarta.validation.constraints.NotBlank;

public record InvitationAcceptDTO(@NotBlank String token) {
}
