package com.projecty.platform.modules.member.internal.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import com.projecty.platform.common.constants.AppConstants;
import com.projecty.platform.common.dto.AppResponse;
import com.projecty.platform.common.security.annotation.RequirePermission;
import com.projecty.platform.modules.member.internal.dto.MemberProfileUpdateDTO;
import com.projecty.platform.modules.member.internal.dto.MemberResponseDTO;
import com.projecty.platform.modules.member.internal.dto.MyMembershipResponseDTO;
import com.projecty.platform.modules.member.internal.dto.ProfileResponseDTO;
import com.projecty.platform.modules.member.internal.service.MemberService;
import com.projecty.platform.modules.shared.constants.PermissionConstants;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The people in the request's portal (X-Portal-Id) and the caller's own membership.
 */
@RestController
@RequestMapping("/api/portal")
public class MemberController {

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	/**
	 * My profile and permissions here; the UI hides what I can't do (USR-US-06).
	 */
	@GetMapping("/me")
	public AppResponse<MyMembershipResponseDTO> me(@AuthenticationPrincipal Jwt jwt) {
		return AppResponse.ok(this.memberService.getMine(UUID.fromString(jwt.getSubject())));
	}

	@GetMapping("/members")
	@RequirePermission(PermissionConstants.USERS_VIEW)
	public AppResponse<List<MemberResponseDTO>> members() {
		return AppResponse.ok(this.memberService.listMembers());
	}

	@GetMapping("/profiles")
	@RequirePermission(PermissionConstants.USERS_VIEW)
	public AppResponse<List<ProfileResponseDTO>> profiles() {
		return AppResponse.ok(this.memberService.listProfiles());
	}

	@PatchMapping("/members/{userId}")
	@RequirePermission(PermissionConstants.USERS_MANAGE)
	public AppResponse<MemberResponseDTO> changeProfile(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId,
			@Valid @RequestBody MemberProfileUpdateDTO request) {
		return AppResponse.ok(this.memberService.changeProfile(UUID.fromString(jwt.getSubject()),
				jwt.getClaimAsString(AppConstants.CLAIM_NAME), userId, request.profileId()));
	}

	@PostMapping("/members/{userId}/deactivate")
	@RequirePermission(PermissionConstants.USERS_MANAGE)
	public AppResponse<MemberResponseDTO> deactivate(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId) {
		return AppResponse.ok(this.memberService.deactivate(UUID.fromString(jwt.getSubject()),
				jwt.getClaimAsString(AppConstants.CLAIM_NAME), userId));
	}

	@PostMapping("/members/{userId}/reactivate")
	@RequirePermission(PermissionConstants.USERS_MANAGE)
	public AppResponse<MemberResponseDTO> reactivate(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId) {
		return AppResponse.ok(this.memberService.reactivate(UUID.fromString(jwt.getSubject()),
				jwt.getClaimAsString(AppConstants.CLAIM_NAME), userId));
	}

}
