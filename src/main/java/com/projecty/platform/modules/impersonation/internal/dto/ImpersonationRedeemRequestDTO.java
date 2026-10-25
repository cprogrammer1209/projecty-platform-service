package com.projecty.platform.modules.impersonation.internal.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * The one-time code from the launch link ({frontend-url}/impersonate?code=...).
 */
public record ImpersonationRedeemRequestDTO(@NotBlank String code) {
}
