package com.projecty.platform.modules.impersonation.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import com.projecty.platform.common.dto.AppResponse;
import com.projecty.platform.modules.impersonation.internal.dto.ImpersonationCreateDTO;
import com.projecty.platform.modules.impersonation.internal.dto.ImpersonationStartResponseDTO;
import com.projecty.platform.modules.impersonation.internal.service.ImpersonationService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * "Sign in as" from the admin console (platform admins only, see SecurityConfig). The customer app redeems
 * the returned link through /api/auth/impersonation/redeem.
 */
@RestController
public class AdminImpersonationController {

	private final ImpersonationService impersonationService;

	public AdminImpersonationController(ImpersonationService impersonationService) {
		this.impersonationService = impersonationService;
	}

	@PostMapping("/api/admin/impersonations")
	@ResponseStatus(HttpStatus.CREATED)
	public AppResponse<ImpersonationStartResponseDTO> start(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody ImpersonationCreateDTO request) {
		return AppResponse.ok(this.impersonationService.start(UUID.fromString(jwt.getSubject()), request));
	}

}
