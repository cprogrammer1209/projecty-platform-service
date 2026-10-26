package com.projecty.platform.modules.member.internal.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import com.projecty.platform.common.constants.AppConstants;
import com.projecty.platform.common.dto.AppResponse;
import com.projecty.platform.common.security.annotation.RequirePermission;
import com.projecty.platform.modules.member.internal.dto.InvitationCreateDTO;
import com.projecty.platform.modules.member.internal.dto.InvitationResponseDTO;
import com.projecty.platform.modules.member.internal.service.InvitationService;
import com.projecty.platform.modules.shared.constants.PermissionConstants;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Invitations of the request's portal (USR-US-01). Admins only.
 */
@RestController
@RequestMapping("/api/portal/invitations")
public class InvitationController {

	private final InvitationService invitationService;

	public InvitationController(InvitationService invitationService) {
		this.invitationService = invitationService;
	}

	@GetMapping
	@RequirePermission(PermissionConstants.USERS_MANAGE)
	public AppResponse<List<InvitationResponseDTO>> list() {
		return AppResponse.ok(this.invitationService.listPending());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@RequirePermission(PermissionConstants.USERS_MANAGE)
	public AppResponse<InvitationResponseDTO> invite(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody InvitationCreateDTO request) {
		return AppResponse.ok(this.invitationService.invite(UUID.fromString(jwt.getSubject()),
				jwt.getClaimAsString(AppConstants.CLAIM_NAME), request));
	}

	@DeleteMapping("/{invitationId}")
	@RequirePermission(PermissionConstants.USERS_MANAGE)
	public AppResponse<Void> revoke(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID invitationId) {
		this.invitationService.revoke(UUID.fromString(jwt.getSubject()), jwt.getClaimAsString(AppConstants.CLAIM_NAME),
				invitationId);
		return AppResponse.ok();
	}

}
