package com.projecty.platform.modules.portal.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import com.projecty.platform.common.dto.AppResponse;
import com.projecty.platform.modules.portal.internal.dto.PortalCreateDTO;
import com.projecty.platform.modules.portal.internal.dto.PortalResponseDTO;
import com.projecty.platform.modules.portal.internal.dto.SlugAvailabilityResponseDTO;
import com.projecty.platform.modules.portal.internal.service.PortalService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portals")
public class PortalController {

	private final PortalService portalService;

	public PortalController(PortalService portalService) {
		this.portalService = portalService;
	}

	/**
	 * Creates a portal owned by the caller. The new portal is not in the caller's current access token, so the
	 * client should call {@code POST /api/auth/refresh} afterwards.
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AppResponse<PortalResponseDTO> create(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody PortalCreateDTO request) {
		return AppResponse.ok(this.portalService.create(UUID.fromString(jwt.getSubject()), request));
	}

	@GetMapping("/slug-availability")
	public AppResponse<SlugAvailabilityResponseDTO> slugAvailability(@RequestParam String slug) {
		return AppResponse.ok(this.portalService.checkSlug(slug));
	}

}
