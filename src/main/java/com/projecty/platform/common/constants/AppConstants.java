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

	/**
	 * The portal a customer request acts in (PTL-US-04). Must be one of the token's {@code portals}.
	 */
	public static final String HEADER_PORTAL_ID = "X-Portal-Id";

	/**
	 * Access token claim listing the ids of the portals the user may act in.
	 */
	public static final String CLAIM_PORTALS = "portals";

	/**
	 * Access token claim with the user's full name.
	 */
	public static final String CLAIM_NAME = "name";

	/**
	 * Request attribute holding the verified portal id (set by PortalContextFilter).
	 */
	public static final String REQUEST_ATTRIBUTE_PORTAL_ID = "projecty.portalId";

	public static final String MESSAGE_PORTAL_ID_INVALID = "X-Portal-Id must be a portal id.";

	public static final String ERROR_PORTAL_REQUIRED = "PORTAL_REQUIRED";

	public static final String ERROR_PORTAL_ACCESS_DENIED = "PORTAL_ACCESS_DENIED";

	public static final String MESSAGE_PORTAL_REQUIRED = "Choose a portal for this request (X-Portal-Id header).";

	public static final String MESSAGE_PORTAL_ACCESS_DENIED = "You don't have access to this portal.";

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
