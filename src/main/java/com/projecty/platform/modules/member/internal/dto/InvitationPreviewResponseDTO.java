package com.projecty.platform.modules.member.internal.dto;

/**
 * What the invite link shows before accepting. Only the token's holder can see it.
 * @param valid false if expired, withdrawn or already used
 * @param accountExists whether to offer "sign in to accept" instead of creating an account
 */
public record InvitationPreviewResponseDTO(String portalName, String email, String profileName,
		String invitedByName, boolean valid, boolean accountExists) {
}
