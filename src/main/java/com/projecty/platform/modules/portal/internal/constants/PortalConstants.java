package com.projecty.platform.modules.portal.internal.constants;

import java.util.List;
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

	/**
	 * PTL-US-02 defaults for a new portal. The time zone comes from the creator's browser when sent.
	 */
	public static final String DEFAULT_TIME_ZONE = "UTC";

	public static final String DEFAULT_DATE_FORMAT = "MMM d, yyyy";

	public static final String DEFAULT_CURRENCY = "USD";

	/**
	 * Date patterns a portal can choose (Angular DatePipe / Java DateTimeFormatter syntax).
	 */
	public static final List<String> ALLOWED_DATE_FORMATS = List.of("MMM d, yyyy", "d MMM yyyy", "dd/MM/yyyy",
			"MM/dd/yyyy", "yyyy-MM-dd");

	public static final String LABEL_TIME_ZONE = "Time zone";

	public static final String LABEL_DATE_FORMAT = "Date format";

	public static final String LABEL_CURRENCY = "Currency";

	public static final String LABEL_WEEK_START = "Week start";

	public static final String FIELD_TIME_ZONE = "timeZone";

	public static final String FIELD_DATE_FORMAT = "dateFormat";

	public static final String FIELD_CURRENCY = "currency";

	public static final String ERROR_PORTAL_NOT_FOUND = "PORTAL_NOT_FOUND";

	public static final String MESSAGE_PORTAL_NOT_FOUND = "Portal not found.";

	public static final String MESSAGE_TIME_ZONE_INVALID = "Choose a valid time zone.";

	public static final String MESSAGE_DATE_FORMAT_INVALID = "Choose one of the offered date formats.";

	public static final String MESSAGE_CURRENCY_INVALID = "Choose a valid currency code.";

	public static final String MESSAGE_SETTINGS_FORBIDDEN = "Only the portal owner can change portal settings.";

	private PortalConstants() {
	}

}
