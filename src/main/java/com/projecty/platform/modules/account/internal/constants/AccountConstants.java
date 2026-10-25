package com.projecty.platform.modules.account.internal.constants;

public final class AccountConstants {

	/**
	 * SEC-BR-01.
	 */
	public static final int PASSWORD_MIN_LENGTH = 10;

	public static final int PASSWORD_MAX_LENGTH = 128;

	public static final String COMMON_PASSWORDS_RESOURCE = "security/common-passwords.txt";

	public static final String COMMON_PASSWORDS_COMMENT_PREFIX = "#";

	/**
	 * The part of the email before "@" is only checked against the password when it is at least this long.
	 */
	public static final int EMAIL_NAME_MIN_LENGTH = 4;

	/**
	 * Hashed once at startup and checked when the email is unknown, so the response takes as long as for a
	 * real account.
	 */
	public static final String TIMING_DUMMY_PASSWORD = "not-a-real-password";

	public static final String VERIFY_EMAIL_PAGE = "verify-email";

	public static final String RESET_PASSWORD_PAGE = "reset-password";

	public static final String ERROR_EMAIL_TAKEN = "EMAIL_TAKEN";

	public static final String ERROR_WEAK_PASSWORD = "WEAK_PASSWORD";

	public static final String ERROR_TOKEN_INVALID = "TOKEN_INVALID_OR_EXPIRED";

	public static final String ERROR_INVALID_CREDENTIALS = "INVALID_CREDENTIALS";

	public static final String ERROR_ACCOUNT_LOCKED = "ACCOUNT_LOCKED";

	public static final String ERROR_ACCOUNT_DISABLED = "ACCOUNT_DISABLED";

	public static final String ERROR_EMAIL_NOT_VERIFIED = "EMAIL_NOT_VERIFIED";

	public static final String ERROR_USER_NOT_FOUND = "USER_NOT_FOUND";

	public static final String MESSAGE_EMAIL_TAKEN = "An account with this email already exists.";

	public static final String MESSAGE_PASSWORD_TOO_SHORT = "Password must be at least " + PASSWORD_MIN_LENGTH
			+ " characters.";

	public static final String MESSAGE_PASSWORD_TOO_LONG = "Password must be at most " + PASSWORD_MAX_LENGTH
			+ " characters.";

	public static final String MESSAGE_PASSWORD_COMMON = "This password is too common. Choose a less predictable one.";

	public static final String MESSAGE_PASSWORD_CONTAINS_EMAIL = "Password must not contain your email address.";

	public static final String MESSAGE_TOKEN_INVALID = "This link is invalid or has expired.";

	public static final String MESSAGE_INVALID_CREDENTIALS = "Incorrect email or password.";

	public static final String MESSAGE_ACCOUNT_LOCKED = "Too many failed attempts. Try again later or reset your password.";

	public static final String MESSAGE_ACCOUNT_DISABLED = "This account has been disabled.";

	public static final String MESSAGE_EMAIL_NOT_VERIFIED = "Verify your email address before logging in.";

	public static final String MESSAGE_USER_NOT_FOUND = "User no longer exists.";

	public static final String DETAIL_LOCKED_UNTIL = "lockedUntil";

	public static final String EMAIL_SIGNATURE = "— ProjectY";

	public static final String EMAIL_VERIFY_SUBJECT = "Verify your email for ProjectY";

	public static final String EMAIL_VERIFY_INTRO = "Confirm your email address to finish creating your ProjectY account.";

	public static final String EMAIL_VERIFY_ACTION = "Verify email";

	public static final String EMAIL_VERIFY_FOOTER = "This link expires in 24 hours.";

	public static final String EMAIL_RESET_SUBJECT = "Reset your ProjectY password";

	public static final String EMAIL_RESET_INTRO = "We received a request to reset your password.";

	public static final String EMAIL_RESET_ACTION = "Reset password";

	public static final String EMAIL_RESET_FOOTER = "This link expires in 1 hour. If you didn't ask for this, you can ignore this email.";

	private AccountConstants() {
	}

}
