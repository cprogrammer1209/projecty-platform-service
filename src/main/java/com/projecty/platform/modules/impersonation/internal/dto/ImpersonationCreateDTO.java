package com.projecty.platform.modules.impersonation.internal.dto;

import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.projecty.platform.modules.impersonation.internal.constants.ImpersonationConstants;

/**
 * The impersonation form. {@code acknowledged}: the admin confirmed this is an authorized, audited action.
 */
public record ImpersonationCreateDTO(@NotNull UUID portalId, @NotNull UUID targetUserId,
		@NotNull Integer durationMinutes, @NotBlank String reason, @NotBlank @Size(max = ImpersonationConstants.COMMENT_MAX_LENGTH) String comment,
		@AssertTrue(message = "Confirm this is an authorized support action.") boolean acknowledged) {
}
