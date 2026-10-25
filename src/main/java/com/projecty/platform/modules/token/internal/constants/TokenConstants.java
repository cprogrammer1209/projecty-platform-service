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

	public static final String JWKS_PATH = "/.well-known/jwks.json";

	private TokenConstants() {
	}

}
