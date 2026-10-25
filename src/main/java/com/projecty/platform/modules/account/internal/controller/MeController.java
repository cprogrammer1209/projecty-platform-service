package com.projecty.platform.modules.account.internal.controller;

import java.util.UUID;

import com.projecty.platform.common.dto.AppResponse;
import com.projecty.platform.modules.account.internal.dto.MeResponseDTO;
import com.projecty.platform.modules.account.internal.service.UserService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

	private final UserService userService;

	public MeController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/api/me")
	public AppResponse<MeResponseDTO> me(@AuthenticationPrincipal Jwt jwt) {
		return AppResponse.ok(this.userService.getMe(UUID.fromString(jwt.getSubject())));
	}

}
