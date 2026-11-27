package com.projecty.platform.modules.admin.internal.controller;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.projecty.platform.common.constants.RateLimitConstants;
import com.projecty.platform.common.dto.AppResponse;
import com.projecty.platform.common.security.annotation.RateLimit;
import com.projecty.platform.modules.admin.api.dto.AdminResponseDTO;
import com.projecty.platform.modules.admin.internal.constants.AdminConstants;
import com.projecty.platform.modules.admin.internal.dto.AdminAccessTokenResponseDTO;
import com.projecty.platform.modules.admin.internal.dto.AdminLoginRequestDTO;
import com.projecty.platform.modules.admin.internal.dto.AdminSessionTokensDTO;
import com.projecty.platform.modules.admin.internal.helper.AdminRefreshCookieHelper;
import com.projecty.platform.modules.admin.internal.mapper.AdminMapper;
import com.projecty.platform.modules.admin.internal.service.AdminUserService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin console sign-in. Same shape as the customer endpoints, with its own cookie and token audience.
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

	private final AdminUserService adminUserService;

	private final AdminRefreshCookieHelper cookieHelper;

	private final AdminMapper mapper;

	public AdminAuthController(AdminUserService adminUserService, AdminRefreshCookieHelper cookieHelper,
			AdminMapper mapper) {
		this.adminUserService = adminUserService;
		this.cookieHelper = cookieHelper;
		this.mapper = mapper;
	}

	@PostMapping("/login")
	@RateLimit(RateLimitConstants.LOGIN)
	public ResponseEntity<AppResponse<AdminAccessTokenResponseDTO>> login(
			@Valid @RequestBody AdminLoginRequestDTO request, HttpServletRequest http) {
		return withCookie(this.adminUserService.login(request.email(), request.password(),
				http.getHeader(HttpHeaders.USER_AGENT), http.getRemoteAddr()));
	}

	@PostMapping("/refresh")
	@RateLimit(RateLimitConstants.REFRESH)
	public ResponseEntity<AppResponse<AdminAccessTokenResponseDTO>> refresh(
			@CookieValue(name = AdminConstants.REFRESH_COOKIE_NAME, required = false) String refreshToken) {
		return withCookie(this.adminUserService.refresh(refreshToken));
	}

	@PostMapping("/logout")
	public ResponseEntity<AppResponse<Void>> logout(
			@CookieValue(name = AdminConstants.REFRESH_COOKIE_NAME, required = false) String refreshToken) {
		this.adminUserService.logout(refreshToken);
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, this.cookieHelper.clear().toString())
			.body(AppResponse.ok());
	}

	@GetMapping("/me")
	public AppResponse<AdminResponseDTO> me(@AuthenticationPrincipal Jwt jwt) {
		return AppResponse.ok(this.adminUserService.getMe(UUID.fromString(jwt.getSubject())));
	}

	private ResponseEntity<AppResponse<AdminAccessTokenResponseDTO>> withCookie(AdminSessionTokensDTO tokens) {
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE,
					this.cookieHelper.create(tokens.refreshToken(), tokens.refreshExpiresAt()).toString())
			.body(AppResponse.ok(this.mapper.toResponse(tokens)));
	}

}
