package com.projecty.platform.common.constants;

/**
 * App-wide constants: error codes not owned by any module.
 */
public final class AppConstants {

	public static final String ERROR_VALIDATION_FAILED = "VALIDATION_FAILED";

	public static final String ERROR_UNAUTHORIZED = "UNAUTHORIZED";

	public static final String ERROR_FORBIDDEN = "FORBIDDEN";

	public static final String ERROR_INTERNAL = "INTERNAL_ERROR";

	public static final String MESSAGE_VALIDATION_FAILED = "Some fields are invalid.";

	public static final String MESSAGE_UNAUTHORIZED = "Authentication is required.";

	public static final String MESSAGE_FORBIDDEN = "You don't have permission to do this.";

	public static final String MESSAGE_INTERNAL = "Something went wrong. Please try again.";

	private AppConstants() {
	}

}
