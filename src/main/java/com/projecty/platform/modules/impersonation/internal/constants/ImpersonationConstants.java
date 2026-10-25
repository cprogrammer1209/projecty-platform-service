package com.projecty.platform.modules.impersonation.internal.constants;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ImpersonationConstants {

	/**
	 * Session lengths offered in the admin console: 15 min, 1 h, 4 h, 12 h, 24 h.
	 */
	public static final List<Integer> ALLOWED_DURATION_MINUTES = List.of(15, 60, 240, 720, 1440);

	public static final List<String> ALLOWED_REASONS = List.of("Account setup", "Demo", "Configuration",
			"Testing");

	public static final int COMMENT_MAX_LENGTH = 1000;

	/**
	 * Times in audit details, e.g. "Oct 25, 2026 20:11 UTC".
	 */
	public static final DateTimeFormatter AUDIT_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm 'UTC'")
		.withZone(ZoneOffset.UTC);

	/**
	 * Customer app page that redeems the launch code: {frontend-url}/impersonate?code=...
	 */
	public static final String LAUNCH_PAGE = "impersonate";

	public static final String ERROR_INVALID_REQUEST = "IMPERSONATION_INVALID";

	public static final String ERROR_TARGET_NOT_ELIGIBLE = "IMPERSONATION_TARGET_NOT_ELIGIBLE";

	public static final String ERROR_LINK_INVALID = "IMPERSONATION_LINK_INVALID";

	public static final String MESSAGE_INVALID_DURATION = "Choose one of the offered session durations.";

	public static final String MESSAGE_INVALID_REASON = "Choose one of the offered reasons.";

	public static final String MESSAGE_TARGET_NOT_ELIGIBLE = "Only active users of an existing tenant can be impersonated.";

	public static final String MESSAGE_LINK_INVALID = "This support link is invalid or has expired.";

	private ImpersonationConstants() {
	}

}
