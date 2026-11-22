package com.projecty.platform.modules.shared.constants;

/**
 * Permission names, used in {@code @RequirePermission} by every feature and granted to profiles by the member
 * module. The project-level matrix (module × action × scope) adds more in M1; the PROJECTS_* and CUSTOM_FIELDS_*
 * ones are enforced by the Work service, which keeps its own copy of each member's permissions from member events.
 */
public final class PermissionConstants {

	public static final String PORTAL_SETTINGS_MANAGE = "PORTAL_SETTINGS_MANAGE";

	public static final String USERS_VIEW = "USERS_VIEW";

	public static final String USERS_MANAGE = "USERS_MANAGE";

	public static final String AUDIT_VIEW = "AUDIT_VIEW";

	public static final String PROJECTS_VIEW = "PROJECTS_VIEW";

	public static final String PROJECTS_CREATE = "PROJECTS_CREATE";

	public static final String PROJECTS_EDIT = "PROJECTS_EDIT";

	public static final String PROJECTS_DELETE = "PROJECTS_DELETE";

	public static final String CUSTOM_FIELDS_VIEW = "CUSTOM_FIELDS_VIEW";

	public static final String CUSTOM_FIELDS_MANAGE = "CUSTOM_FIELDS_MANAGE";

	private PermissionConstants() {
	}

}
