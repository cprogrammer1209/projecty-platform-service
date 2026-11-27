package com.projecty.platform.modules.token.internal.constants;

public final class TokenConstants {

	public static final String KEY_ALGORITHM = "RSA";

	public static final int RSA_KEY_SIZE = 2048;

	public static final String CLAIM_EMAIL = "email";

	public static final String CLAIM_SESSION_ID = "sid";

	public static final String CLAIM_ROLE = "role";

	/**
	 * RFC 8693 actor claim: the platform admin acting as the subject.
	 */
	public static final String CLAIM_ACTOR = "act";

	public static final String CLAIM_ACTOR_TYPE = "typ";

	public static final String ACTOR_TYPE_ADMIN = "admin";

	/**
	 * The support session: its id and when it ends (epoch seconds).
	 */
	public static final String CLAIM_IMPERSONATION = "imp";

	public static final String CLAIM_ID = "id";

	public static final String CLAIM_EXPIRES = "exp";

	/**
	 * Key of a denied session: {@code denylist:sid:{sessionId}}. Shared contract: this service writes it, and
	 * work-service reads the same key to turn away access tokens of a session that has ended.
	 */
	public static final String DENYLIST_KEY_PREFIX = "denylist:sid:";

	public static final String DENYLIST_VALUE = "1";

	public static final String ERROR_SESSION_ENDED = "invalid_token";

	public static final String MESSAGE_SESSION_ENDED = "The session of this token has ended.";

	public static final String JWKS_PATH = "/.well-known/jwks.json";

	private TokenConstants() {
	}

}
