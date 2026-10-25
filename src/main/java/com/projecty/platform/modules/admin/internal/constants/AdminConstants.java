package com.projecty.platform.modules.admin.internal.constants;

public final class AdminConstants {

	/**
	 * A different name and path from the customer cookie, so the two apps don't overwrite each other's session
	 * on the same host.
	 */
	public static final String REFRESH_COOKIE_NAME = "py_admin_refresh";

	public static final String REFRESH_COOKIE_PATH = "/api/admin/auth";

	public static final String REFRESH_COOKIE_SAME_SITE = "Strict";

	public static final String TOKEN_TYPE = "Bearer";

	public static final int USER_AGENT_MAX_LENGTH = 512;

	public static final int BOOTSTRAP_PASSWORD_MIN_LENGTH = 10;

	/**
	 * {@code PortalMemberResponseDTO.status} of a member who can use the portal.
	 */
	public static final String MEMBER_STATUS_ACTIVE = "ACTIVE";

	public static final String TIMING_DUMMY_PASSWORD = "not-a-real-password";

	public static final String ERROR_INVALID_CREDENTIALS = "INVALID_CREDENTIALS";

	public static final String ERROR_ACCOUNT_LOCKED = "ACCOUNT_LOCKED";

	public static final String ERROR_ACCOUNT_DISABLED = "ACCOUNT_DISABLED";

	public static final String ERROR_SESSION_EXPIRED = "SESSION_EXPIRED";

	public static final String ERROR_TENANT_NOT_FOUND = "TENANT_NOT_FOUND";

	public static final String DETAIL_LOCKED_UNTIL = "lockedUntil";

	public static final String MESSAGE_INVALID_CREDENTIALS = "Email or password is incorrect.";

	public static final String MESSAGE_ACCOUNT_LOCKED = "Too many failed attempts. Try again later.";

	public static final String MESSAGE_ACCOUNT_DISABLED = "This admin account is disabled.";

	public static final String MESSAGE_SESSION_EXPIRED = "Your admin session has ended. Sign in again.";

	public static final String MESSAGE_TENANT_NOT_FOUND = "Tenant not found.";

	private AdminConstants() {
	}

}
