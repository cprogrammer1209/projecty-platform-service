package com.projecty.platform.modules.member.internal.constants;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import com.projecty.platform.modules.shared.constants.PermissionConstants;

public final class MemberConstants {

	public static final String PROFILE_ADMIN = "ADMIN";

	public static final String PROFILE_MANAGER = "MANAGER";

	public static final String PROFILE_EMPLOYEE = "EMPLOYEE";

	public static final String PROFILE_CLIENT = "CLIENT";

	/**
	 * The system profiles every portal gets (same as Flyway V5). Permissions are fixed until custom profiles
	 * (V1). The PROJECTS_* permissions are enforced by the Work service (Flyway V8 added them to existing portals; V9 added CUSTOM_FIELDS_*, V10 added TASKS_*).
	 */
	public static final List<ProfileDefinition> SYSTEM_PROFILES = List.of(
			new ProfileDefinition(PROFILE_ADMIN, "Admin", "Everything, including portal settings and users.",
					Set.of(PermissionConstants.PORTAL_SETTINGS_MANAGE, PermissionConstants.USERS_VIEW,
							PermissionConstants.USERS_MANAGE, PermissionConstants.AUDIT_VIEW,
							PermissionConstants.PROJECTS_VIEW, PermissionConstants.PROJECTS_CREATE,
							PermissionConstants.PROJECTS_EDIT, PermissionConstants.PROJECTS_DELETE,
							PermissionConstants.CUSTOM_FIELDS_VIEW, PermissionConstants.CUSTOM_FIELDS_MANAGE,
							PermissionConstants.TASKS_VIEW, PermissionConstants.TASKS_CREATE, PermissionConstants.TASKS_EDIT,
							PermissionConstants.TASKS_DELETE)),
			new ProfileDefinition(PROFILE_MANAGER, "Manager", "Runs projects. Sees the people in the portal.",
					Set.of(PermissionConstants.USERS_VIEW, PermissionConstants.PROJECTS_VIEW,
							PermissionConstants.PROJECTS_CREATE, PermissionConstants.PROJECTS_EDIT,
							PermissionConstants.PROJECTS_DELETE, PermissionConstants.CUSTOM_FIELDS_VIEW,
							PermissionConstants.TASKS_VIEW, PermissionConstants.TASKS_CREATE, PermissionConstants.TASKS_EDIT,
							PermissionConstants.TASKS_DELETE)),
			new ProfileDefinition(PROFILE_EMPLOYEE, "Employee",
					"Works on assigned items. Sees the people in the portal.",
					Set.of(PermissionConstants.USERS_VIEW, PermissionConstants.PROJECTS_VIEW,
							PermissionConstants.CUSTOM_FIELDS_VIEW, PermissionConstants.TASKS_VIEW,
							PermissionConstants.TASKS_CREATE, PermissionConstants.TASKS_EDIT)),
			new ProfileDefinition(PROFILE_CLIENT, "Client", "External user. Sees only what is shared with them.",
					Set.of(PermissionConstants.PROJECTS_VIEW, PermissionConstants.CUSTOM_FIELDS_VIEW,
							PermissionConstants.TASKS_VIEW)));

	public static final String INVITE_PAGE = "invite";

	public static final String ERROR_PORTAL_LIMIT = "PORTAL_LIMIT";

	public static final String ERROR_MEMBER_NOT_FOUND = "MEMBER_NOT_FOUND";

	public static final String ERROR_PROFILE_NOT_FOUND = "PROFILE_NOT_FOUND";

	public static final String ERROR_OWNER_LOCKED = "OWNER_LOCKED";

	public static final String ERROR_LAST_ADMIN = "LAST_ADMIN";

	public static final String ERROR_ALREADY_MEMBER = "ALREADY_MEMBER";

	public static final String ERROR_INVITATION_INVALID = "INVITATION_INVALID";

	public static final String ERROR_INVITATION_NOT_FOUND = "INVITATION_NOT_FOUND";

	public static final String ERROR_INVITATION_EMAIL_MISMATCH = "INVITATION_EMAIL_MISMATCH";

	public static final String ERROR_ACCOUNT_EXISTS = "ACCOUNT_EXISTS";

	public static final String MESSAGE_MEMBER_NOT_FOUND = "This person isn't a member of the portal.";

	public static final String MESSAGE_PROFILE_NOT_FOUND = "Choose one of the portal's profiles.";

	public static final String MESSAGE_OWNER_LOCKED = "The portal owner always stays an active Admin.";

	public static final String MESSAGE_LAST_ADMIN = "A portal needs at least one active Admin.";

	public static final String MESSAGE_ALREADY_MEMBER = "This person is already a member of the portal.";

	public static final String MESSAGE_INVITATION_INVALID = "This invitation has expired or was withdrawn. Ask for a new one.";

	public static final String MESSAGE_INVITATION_NOT_FOUND = "Invitation not found.";

	public static final String MESSAGE_INVITATION_EMAIL_MISMATCH = "This invitation was sent to a different email address.";

	public static final String MESSAGE_ACCOUNT_EXISTS = "An account already exists for this email. Sign in to accept.";

	public static final String EMAIL_INVITE_SUBJECT = "You're invited to %s on ProjectY";

	public static final String EMAIL_INVITE_INTRO = "%s invited you to join %s on ProjectY as %s.";

	public static final String EMAIL_INVITE_ACTION = "Accept invitation";

	public static final String EMAIL_INVITE_FOOTER = "This invitation expires in 7 days.";

	public static final String EMAIL_SIGNATURE = "— ProjectY";

	/** How long the (fixed) profile list of a portal may be cached. */
	public static final Duration PROFILES_CACHE_TTL = Duration.ofMinutes(10);

	/** Cached "is this user an active member, with which permissions" answer, one key per portal and user. */
	public static final String ACCESS_KEY_PREFIX = "platform:access:";

	public static final String ACCESS_ACTIVE = "A";

	public static final String ACCESS_INACTIVE = "I";

	public static final String ACCESS_SEPARATOR = ":";

	public static final String PERMISSION_SEPARATOR = ",";

	private MemberConstants() {
	}

	public record ProfileDefinition(String key, String name, String description, Set<String> permissions) {
	}

}
