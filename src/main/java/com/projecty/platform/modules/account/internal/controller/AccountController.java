package com.projecty.platform.modules.account.internal.controller;

import jakarta.validation.Valid;

import com.projecty.platform.common.constants.RateLimitConstants;
import com.projecty.platform.common.dto.AppResponse;
import com.projecty.platform.common.security.annotation.RateLimit;
import com.projecty.platform.modules.account.internal.dto.EmailRequestDTO;
import com.projecty.platform.modules.account.internal.dto.PasswordResetRequestDTO;
import com.projecty.platform.modules.account.internal.dto.SignupCreateDTO;
import com.projecty.platform.modules.account.internal.dto.VerifyEmailRequestDTO;
import com.projecty.platform.modules.account.internal.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public account endpoints: signup, email verification and password reset.
 */
@RestController
@RequestMapping("/api/auth")
public class AccountController {

	private final UserService userService;

	public AccountController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	@RateLimit(RateLimitConstants.SIGNUP)
	public AppResponse<Void> signup(@Valid @RequestBody SignupCreateDTO request) {
		this.userService.signup(request);
		return AppResponse.ok();
	}

	@PostMapping("/verify-email")
	@RateLimit(RateLimitConstants.TOKEN)
	public AppResponse<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequestDTO request) {
		this.userService.verifyEmail(request.token());
		return AppResponse.ok();
	}

	@PostMapping("/resend-verification")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@RateLimit(RateLimitConstants.EMAIL)
	public AppResponse<Void> resendVerification(@Valid @RequestBody EmailRequestDTO request) {
		this.userService.resendVerification(request.email());
		return AppResponse.ok();
	}

	@PostMapping("/forgot-password")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@RateLimit(RateLimitConstants.EMAIL)
	public AppResponse<Void> forgotPassword(@Valid @RequestBody EmailRequestDTO request) {
		this.userService.requestPasswordReset(request.email());
		return AppResponse.ok();
	}

	@PostMapping("/reset-password")
	@RateLimit(RateLimitConstants.TOKEN)
	public AppResponse<Void> resetPassword(@Valid @RequestBody PasswordResetRequestDTO request) {
		this.userService.resetPassword(request.token(), request.newPassword());
		return AppResponse.ok();
	}

}
