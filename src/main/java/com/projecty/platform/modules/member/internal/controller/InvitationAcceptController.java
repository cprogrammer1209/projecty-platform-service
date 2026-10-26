package com.projecty.platform.modules.member.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import com.projecty.platform.common.dto.AppResponse;
import com.projecty.platform.modules.member.internal.dto.AcceptedInvitationResponseDTO;
import com.projecty.platform.modules.member.internal.dto.InvitationAcceptDTO;
import com.projecty.platform.modules.member.internal.dto.InvitationAcceptNewDTO;
import com.projecty.platform.modules.member.internal.dto.InvitationPreviewResponseDTO;
import com.projecty.platform.modules.member.internal.service.InvitationService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The invitee's side (USR-US-02). Preview and "accept with a new account" need no session (under /api/auth);
 * accepting with an existing account needs the user signed in.
 */
@RestController
public class InvitationAcceptController {

	private final InvitationService invitationService;

	public InvitationAcceptController(InvitationService invitationService) {
		this.invitationService = invitationService;
	}

	@GetMapping("/api/auth/invitations/{token}")
	public AppResponse<InvitationPreviewResponseDTO> preview(@PathVariable String token) {
		return AppResponse.ok(this.invitationService.preview(token));
	}

	@PostMapping("/api/auth/invitations/accept-new")
	public AppResponse<AcceptedInvitationResponseDTO> acceptAsNewUser(
			@Valid @RequestBody InvitationAcceptNewDTO request) {
		return AppResponse.ok(this.invitationService.acceptAsNewUser(request));
	}

	@PostMapping("/api/invitations/accept")
	public AppResponse<AcceptedInvitationResponseDTO> accept(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody InvitationAcceptDTO request) {
		return AppResponse.ok(this.invitationService.accept(UUID.fromString(jwt.getSubject()), request.token()));
	}

}
