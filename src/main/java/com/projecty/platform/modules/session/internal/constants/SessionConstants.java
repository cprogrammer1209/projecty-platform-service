package com.projecty.platform.modules.session.internal.constants;

public final class SessionConstants {

	public static final String REFRESH_COOKIE_NAME = "py_refresh";

	/**
	 * The browser sends the refresh cookie only to the auth endpoints.
	 */
	public static final String REFRESH_COOKIE_PATH = "/api/auth";

	public static final String REFRESH_COOKIE_SAME_SITE = "Strict";

	public static final String TOKEN_TYPE = "Bearer";

	public static final int USER_AGENT_MAX_LENGTH = 512;

	public static final String ERROR_SESSION_EXPIRED = "SESSION_EXPIRED";

	public static final String MESSAGE_SESSION_EXPIRED = "Your session has ended. Log in again.";

	public static final String ERROR_NOT_IMPERSONATING = "NOT_IMPERSONATING";

	public static final String MESSAGE_NOT_IMPERSONATING = "This is not a support session.";

	/**
	 * Audit reasons for the end of a support session.
	 */
	public static final String IMPERSONATION_END_BY_ADMIN = "Ended from the support session";

	public static final String IMPERSONATION_END_EXPIRED = "Session time ran out";

	private SessionConstants() {
	}

}
