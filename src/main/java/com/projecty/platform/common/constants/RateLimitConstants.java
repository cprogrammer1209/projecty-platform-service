package com.projecty.platform.common.constants;

/**
 * Names of the limits a controller method can carry with {@code @RateLimit}. The numbers live in
 * {@code projecty.rate-limit.limits.<name>}.
 */
public final class RateLimitConstants {

	/** Password sign-in, customer and admin console. */
	public static final String LOGIN = "login";

	public static final String REFRESH = "refresh";

	public static final String SIGNUP = "signup";

	/** Endpoints that send an email: resend verification and forgot password. */
	public static final String EMAIL = "email";

	/** Endpoints that take a one-time token: verify email, reset password, accept invitation, redeem support session. */
	public static final String TOKEN = "token";

	public static final String KEY_PREFIX = "platform:ratelimit:";

	public static final String KEY_SEPARATOR = ":";

	public static final String ERROR_RATE_LIMITED = "RATE_LIMITED";

	public static final String MESSAGE_RATE_LIMITED = "Too many attempts. Try again in a little while.";

	public static final String HEADER_RETRY_AFTER = "Retry-After";

	public static final String DETAIL_RETRY_AFTER_SECONDS = "retryAfterSeconds";

	private RateLimitConstants() {
	}

}
