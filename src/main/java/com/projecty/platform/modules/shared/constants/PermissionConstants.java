package com.projecty.platform.modules.shared.constants;

/**
 * Permission names, used in {@code @RequirePermission} by every feature and granted to profiles by the member
 * module. The project-level matrix (module × action × scope) adds more in M1.
 */
public final class PermissionConstants {

	public static final String PORTAL_SETTINGS_MANAGE = "PORTAL_SETTINGS_MANAGE";

	public static final String USERS_VIEW = "USERS_VIEW";

	public static final String USERS_MANAGE = "USERS_MANAGE";

	public static final String AUDIT_VIEW = "AUDIT_VIEW";

	private PermissionConstants() {
	}

}
