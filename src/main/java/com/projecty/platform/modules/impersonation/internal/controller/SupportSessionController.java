package com.projecty.platform.modules.impersonation.internal.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.projecty.platform.common.constants.AppConstants;
import com.projecty.platform.common.constants.RateLimitConstants;
import com.projecty.platform.common.dto.AppResponse;
import com.projecty.platform.common.security.annotation.RateLimit;
import com.projecty.platform.common.security.helper.RefreshCookieHelper;
import com.projecty.platform.modules.impersonation.internal.constants.ImpersonationConstants;
import com.projecty.platform.modules.impersonation.internal.dto.ImpersonationRedeemRequestDTO;
import com.projecty.platform.modules.impersonation.internal.dto.SupportSessionTokenResponseDTO;
import com.projecty.platform.modules.impersonation.internal.service.ImpersonationService;
import com.projecty.platform.modules.session.api.dto.IssuedSessionDTO;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The customer-app side of a support session. Under /api/auth so the browser sends the refresh cookie here.
 */
@RestController
@RequestMapping("/api/auth/impersonation")
public class SupportSessionController {

	private final ImpersonationService impersonationService;

	private final RefreshCookieHelper refreshCookieHelper;

	public SupportSessionController(ImpersonationService impersonationService,
			RefreshCookieHelper refreshCookieHelper) {
		this.impersonationService = impersonationService;
		this.refreshCookieHelper = refreshCookieHelper;
	}

	/**
	 * Opens a support session from the admin console's launch link.
	 */
	@PostMapping("/redeem")
	@RateLimit(RateLimitConstants.TOKEN)
	public ResponseEntity<AppResponse<SupportSessionTokenResponseDTO>> redeem(
			@Valid @RequestBody ImpersonationRedeemRequestDTO request, HttpServletRequest http) {
		IssuedSessionDTO session = this.impersonationService.startSupportSession(request.code(),
				http.getHeader(HttpHeaders.USER_AGENT), http.getRemoteAddr());
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE,
					this.refreshCookieHelper.create(session.refreshToken(), session.refreshExpiresAt()).toString())
			.body(AppResponse.ok(new SupportSessionTokenResponseDTO(session.accessToken(),
					ImpersonationConstants.TOKEN_TYPE, session.expiresInSeconds())));
	}

	@PostMapping("/end")
	public ResponseEntity<AppResponse<Void>> end(
			@CookieValue(name = AppConstants.REFRESH_COOKIE_NAME, required = false) String refreshToken) {
		this.impersonationService.endSupportSession(refreshToken);
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, this.refreshCookieHelper.clear().toString())
			.body(AppResponse.ok());
	}

}
