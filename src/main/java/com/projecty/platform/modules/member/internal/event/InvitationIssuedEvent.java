package com.projecty.platform.modules.member.internal.event;

/**
 * Published when an invitation is created. The email is sent once the transaction commits.
 */
public record InvitationIssuedEvent(String email, String portalName, String inviterName, String profileName,
		String token) {
}
