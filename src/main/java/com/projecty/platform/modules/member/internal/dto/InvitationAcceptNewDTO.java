package com.projecty.platform.modules.member.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Accepting as someone without an account: the invitation link proves the email, so the new account is
 * verified at once.
 */
public record InvitationAcceptNewDTO(@NotBlank String token, @NotBlank @Size(max = 200) String fullName,
		@NotBlank String password) {
}
