package com.projecty.platform.modules.session.internal.dto;

import jakarta.validation.constraints.NotBlank;

public record ImpersonationRedeemRequestDTO(@NotBlank String code) {
}
