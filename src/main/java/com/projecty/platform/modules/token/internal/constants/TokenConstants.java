package com.projecty.platform.modules.token.internal.constants;

public final class TokenConstants {

	public static final String KEY_ALGORITHM = "RSA";

	public static final int RSA_KEY_SIZE = 2048;

	public static final String CLAIM_EMAIL = "email";

	public static final String CLAIM_NAME = "name";

	public static final String CLAIM_PORTALS = "portals";

	public static final String CLAIM_SESSION_ID = "sid";

	public static final String JWKS_PATH = "/.well-known/jwks.json";

	private TokenConstants() {
	}

}
