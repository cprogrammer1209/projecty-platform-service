package com.projecty.platform.modules.shared.constants;

/**
 * Keys of cached reads that more than one module touches: the module that reads a value caches it, and the module
 * that changes the data it depends on evicts it by the same key (for example a member change drops the user's
 * {@code /api/me}, which the account module caches).
 */
public final class CacheKeyConstants {

	public static final String SEPARATOR = ":";

	/** One user (id, email, name, active, last sign-in). */
	public static final String USER_KEY = "platform:user:";

	/** The user's {@code /api/me} answer: profile plus the portals they belong to. */
	public static final String ME_KEY = "platform:me:";

	/** One portal's summary, and the current-portal settings answer. */
	public static final String PORTAL_KEY = "platform:portal:";

	public static final String PORTAL_CURRENT_KEY = "platform:portal-current:";

	/** The portals a user is an active member of, and a user's own membership of one portal. */
	public static final String PORTALS_OF_USER_KEY = "platform:portals:";

	public static final String PORTAL_ME_KEY = "platform:portal-me:";

	/** Versions that move when the members, or the pending invitations, of a portal change. */
	public static final String MEMBERS_SCOPE = "platform:members:";

	public static final String INVITATIONS_SCOPE = "platform:invitations:";

	/** Cached list responses of a portal; the version (when there is one) is appended to the key. */
	public static final String MEMBERS_KEY = "platform:resp:members:";

	public static final String PROFILES_KEY = "platform:resp:profiles:";

	public static final String INVITATIONS_KEY = "platform:resp:invitations:";

	private CacheKeyConstants() {
	}

}
