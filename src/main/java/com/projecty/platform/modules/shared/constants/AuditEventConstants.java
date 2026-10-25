package com.projecty.platform.modules.shared.constants;

/**
 * Values for audit events, shared by every module that records them (admin, impersonation, session).
 */
public final class AuditEventConstants {

	public static final String CATEGORY_SECURITY = "SECURITY";

	public static final String CATEGORY_ADMIN = "ADMIN";

	public static final String ACTOR_ADMIN = "ADMIN";

	public static final String ACTOR_USER = "USER";

	public static final String ACTION_ADMIN_SIGNED_IN = "ADMIN_SIGNED_IN";

	public static final String ACTION_IMPERSONATION_REQUESTED = "IMPERSONATION_REQUESTED";

	public static final String ACTION_IMPERSONATION_STARTED = "IMPERSONATION_STARTED";

	public static final String ACTION_IMPERSONATION_ENDED = "IMPERSONATION_ENDED";

	private AuditEventConstants() {
	}

}
