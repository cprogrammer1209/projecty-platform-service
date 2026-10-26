package com.projecty.platform.modules.shared.constants;

/**
 * Business event and aggregate names, shared by the modules that publish events through the outbox
 * (portal, member).
 */
public final class OutboxEventConstants {

	public static final String AGGREGATE_PORTAL = "PORTAL";

	public static final String AGGREGATE_INVITATION = "INVITATION";

	public static final String AGGREGATE_MEMBER = "MEMBER";

	public static final String EVENT_PORTAL_CREATED = "PortalCreated";

	public static final String EVENT_USER_INVITED = "UserInvited";

	public static final String EVENT_USER_ACTIVATED = "UserActivated";

	public static final String EVENT_USER_PROFILE_CHANGED = "UserProfileChanged";

	public static final String EVENT_USER_DEACTIVATED = "UserDeactivated";

	private OutboxEventConstants() {
	}

}
