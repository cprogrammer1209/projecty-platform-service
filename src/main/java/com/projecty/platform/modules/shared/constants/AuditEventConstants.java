package com.projecty.platform.modules.shared.constants;

/**
 * Values for audit events, shared by every module that records them (admin, impersonation, portal, member, account).
 */
public final class AuditEventConstants {

	public static final String CATEGORY_SECURITY = "SECURITY";

	public static final String CATEGORY_ADMIN = "ADMIN";

	public static final String CATEGORY_AUTH = "AUTH";

	public static final String ACTOR_ADMIN = "ADMIN";

	public static final String ACTOR_USER = "USER";

	public static final String ACTION_USER_SIGNED_UP = "USER_SIGNED_UP";

	public static final String ACTION_LOGIN_SUCCEEDED = "LOGIN_SUCCEEDED";

	public static final String ACTION_LOGIN_FAILED = "LOGIN_FAILED";

	public static final String ACTION_ACCOUNT_LOCKED = "ACCOUNT_LOCKED";

	public static final String ACTION_PASSWORD_RESET = "PASSWORD_RESET";

	public static final String ACTION_PORTAL_CREATED = "PORTAL_CREATED";

	public static final String ACTION_ADMIN_SIGNED_IN = "ADMIN_SIGNED_IN";

	public static final String ACTION_IMPERSONATION_REQUESTED = "IMPERSONATION_REQUESTED";

	public static final String ACTION_IMPERSONATION_STARTED = "IMPERSONATION_STARTED";

	public static final String ACTION_IMPERSONATION_ENDED = "IMPERSONATION_ENDED";

	public static final String ACTION_PORTAL_SETTINGS_CHANGED = "PORTAL_SETTINGS_CHANGED";

	public static final String ACTION_USER_INVITED = "USER_INVITED";

	public static final String ACTION_INVITATION_REVOKED = "INVITATION_REVOKED";

	public static final String ACTION_INVITATION_ACCEPTED = "INVITATION_ACCEPTED";

	public static final String ACTION_USER_PROFILE_CHANGED = "USER_PROFILE_CHANGED";

	public static final String ACTION_USER_DEACTIVATED = "USER_DEACTIVATED";

	public static final String ACTION_USER_REACTIVATED = "USER_REACTIVATED";

	private AuditEventConstants() {
	}

}
