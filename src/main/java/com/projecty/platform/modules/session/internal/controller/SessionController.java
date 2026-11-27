package com.projecty.platform.modules.session.internal.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.projecty.platform.common.constants.AppConstants;
import com.projecty.platform.common.constants.RateLimitConstants;
import com.projecty.platform.common.dto.AppResponse;
import com.projecty.platform.common.security.annotation.RateLimit;
import com.projecty.platform.common.security.helper.RefreshCookieHelper;
import com.projecty.platform.modules.session.internal.dto.AccessTokenResponseDTO;
import com.projecty.platform.modules.session.internal.dto.LoginRequestDTO;
import com.projecty.platform.modules.session.internal.dto.SessionTokensDTO;
import com.projecty.platform.modules.session.internal.mapper.SessionMapper;
import com.projecty.platform.modules.session.internal.service.SessionService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login, refresh and logout. Login and refresh return the access token in the body and set the refresh token
 * as an HttpOnly cookie.
 */
@RestController
@RequestMapping("/api/auth")
public class SessionController {

	private final SessionService sessionService;

	private final RefreshCookieHelper refreshCookieHelper;

	private final SessionMapper sessionMapper;

	public SessionController(SessionService sessionService, RefreshCookieHelper refreshCookieHelper,
			SessionMapper sessionMapper) {
		this.sessionService = sessionService;
		this.refreshCookieHelper = refreshCookieHelper;
		this.sessionMapper = sessionMapper;
	}

	@PostMapping("/login")
	@RateLimit(RateLimitConstants.LOGIN)
	public ResponseEntity<AppResponse<AccessTokenResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request,
			HttpServletRequest http) {
		return withCookie(this.sessionService.login(request.email(), request.password(),
				http.getHeader(HttpHeaders.USER_AGENT), http.getRemoteAddr()));
	}

	@PostMapping("/refresh")
	@RateLimit(RateLimitConstants.REFRESH)
	public ResponseEntity<AppResponse<AccessTokenResponseDTO>> refresh(
			@CookieValue(name = AppConstants.REFRESH_COOKIE_NAME, required = false) String refreshToken) {
		return withCookie(this.sessionService.refresh(refreshToken));
	}

	@PostMapping("/logout")
	public ResponseEntity<AppResponse<Void>> logout(
			@CookieValue(name = AppConstants.REFRESH_COOKIE_NAME, required = false) String refreshToken) {
		this.sessionService.logout(refreshToken);
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, this.refreshCookieHelper.clear().toString())
			.body(AppResponse.ok());
	}

	private ResponseEntity<AppResponse<AccessTokenResponseDTO>> withCookie(SessionTokensDTO tokens) {
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE,
					this.refreshCookieHelper.create(tokens.refreshToken(), tokens.refreshExpiresAt()).toString())
			.body(AppResponse.ok(this.sessionMapper.toResponse(tokens)));
	}

}
