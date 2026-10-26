package com.projecty.platform.modules.member.internal.service;

import java.util.List;
import java.util.UUID;

import com.projecty.platform.modules.member.internal.dto.AcceptedInvitationResponseDTO;
import com.projecty.platform.modules.member.internal.dto.InvitationAcceptNewDTO;
import com.projecty.platform.modules.member.internal.dto.InvitationCreateDTO;
import com.projecty.platform.modules.member.internal.dto.InvitationPreviewResponseDTO;
import com.projecty.platform.modules.member.internal.dto.InvitationResponseDTO;

/**
 * Inviting people to the request's portal (USR-US-01) and accepting (USR-US-02).
 */
public interface InvitationService {

	List<InvitationResponseDTO> listPending();

	/**
	 * Invites by email; a pending invitation to the same email is replaced (that's how "resend" works).
	 */
	InvitationResponseDTO invite(UUID actorId, String actorName, InvitationCreateDTO request);

	void revoke(UUID actorId, String actorName, UUID invitationId);

	InvitationPreviewResponseDTO preview(String token);

	/**
	 * For someone without an account: creates a verified account and joins.
	 */
	AcceptedInvitationResponseDTO acceptAsNewUser(InvitationAcceptNewDTO request);

	/**
	 * For a signed-in user whose email matches the invitation.
	 */
	AcceptedInvitationResponseDTO accept(UUID userId, String token);

}
