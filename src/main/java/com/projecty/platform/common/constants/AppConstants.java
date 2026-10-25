package com.projecty.platform.common.constants;

/**
 * App-wide constants: error codes and security roles not owned by any module.
 */
public final class AppConstants {

	/**
	 * Role of a customer access token (audience {@code projecty-api}).
	 */
	public static final String ROLE_USER = "USER";

	/**
	 * Role of an admin console access token (audience {@code projecty-admin}).
	 */
	public static final String ROLE_PLATFORM_ADMIN = "PLATFORM_ADMIN";

	public static final String ROLE_PREFIX = "ROLE_";

	/**
	 * The customer refresh token cookie. The browser sends it only to the auth endpoints.
	 */
	public static final String REFRESH_COOKIE_NAME = "py_refresh";

	public static final String REFRESH_COOKIE_PATH = "/api/auth";

	public static final String REFRESH_COOKIE_SAME_SITE = "Strict";

	public static final String ERROR_VALIDATION_FAILED = "VALIDATION_FAILED";

	public static final String ERROR_UNAUTHORIZED = "UNAUTHORIZED";

	public static final String ERROR_FORBIDDEN = "FORBIDDEN";

	public static final String ERROR_INTERNAL = "INTERNAL_ERROR";

	public static final String MESSAGE_VALIDATION_FAILED = "Some fields are invalid.";

	public static final String MESSAGE_UNAUTHORIZED = "Authentication is required.";

	public static final String MESSAGE_FORBIDDEN = "You don't have permission to do this.";

	public static final String MESSAGE_INTERNAL = "Something went wrong. Please try again.";

	private AppConstants() {
	}

}
