package com.projecty.platform.modules.portal.internal.constants;

import java.util.Set;
import java.util.regex.Pattern;

public final class PortalConstants {

	/**
	 * PTL-BR-02: 3–30 characters, lowercase letters, digits and hyphens, no hyphen at either end.
	 */
	public static final int SLUG_MIN_LENGTH = 3;

	public static final int SLUG_MAX_LENGTH = 30;

	public static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");

	public static final Set<String> RESERVED_SLUGS = Set.of("about", "account", "accounts", "admin", "api", "app",
			"apps", "assets", "auth", "billing", "blog", "cdn", "dashboard", "dev", "docs", "email", "help", "login",
			"logout", "mail", "portal", "portals", "privacy", "projecty", "register", "root", "security", "settings",
			"signup", "staging", "static", "status", "support", "system", "terms", "test", "www");

	/**
	 * Used as the stem of suggestions when the requested slug has too little to work with.
	 */
	public static final String FALLBACK_SLUG = "portal";

	public static final int MAX_SLUG_SUGGESTIONS = 3;

	public static final int SUGGESTION_NUMBERED_FROM = 2;

	public static final int SUGGESTION_NUMBERED_TO = 6;

	public static final int SUGGESTION_RANDOM_MIN = 100;

	public static final int SUGGESTION_RANDOM_MAX = 1000;

	public static final String ERROR_SLUG_INVALID = "SLUG_INVALID";

	public static final String ERROR_SLUG_TAKEN = "SLUG_TAKEN";

	public static final String ERROR_PORTAL_LIMIT = "PORTAL_LIMIT";

	public static final String MESSAGE_SLUG_LENGTH = "Slug must be " + SLUG_MIN_LENGTH + "–" + SLUG_MAX_LENGTH
			+ " characters.";

	public static final String MESSAGE_SLUG_FORMAT = "Use lowercase letters, digits and hyphens, not starting or ending with a hyphen.";

	public static final String MESSAGE_SLUG_RESERVED = "This slug is reserved.";

	public static final String MESSAGE_SLUG_TAKEN = "This slug is already taken.";

	public static final String DETAIL_SUGGESTIONS = "suggestions";

	private PortalConstants() {
	}

}
