package com.projecty.platform.modules.portal.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import com.projecty.platform.common.constants.AppConstants;
import com.projecty.platform.common.dto.AppResponse;
import com.projecty.platform.modules.portal.internal.dto.CurrentPortalResponseDTO;
import com.projecty.platform.modules.portal.internal.dto.PortalSettingsUpdateDTO;
import com.projecty.platform.modules.portal.internal.service.PortalService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The portal named by the request's {@code X-Portal-Id} header: its details and settings (PTL-US-02).
 */
@RestController
@RequestMapping("/api/portal")
public class CurrentPortalController {

	private final PortalService portalService;

	public CurrentPortalController(PortalService portalService) {
		this.portalService = portalService;
	}

	@GetMapping
	public AppResponse<CurrentPortalResponseDTO> get(@AuthenticationPrincipal Jwt jwt) {
		return AppResponse.ok(this.portalService.getCurrent(UUID.fromString(jwt.getSubject())));
	}

	@PatchMapping("/settings")
	public AppResponse<CurrentPortalResponseDTO> updateSettings(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody PortalSettingsUpdateDTO request) {
		return AppResponse.ok(this.portalService.updateSettings(UUID.fromString(jwt.getSubject()),
				jwt.getClaimAsString(AppConstants.CLAIM_NAME), request));
	}

}
