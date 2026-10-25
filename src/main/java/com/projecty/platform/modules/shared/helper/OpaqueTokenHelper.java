package com.projecty.platform.modules.shared.helper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import com.projecty.platform.modules.shared.constants.SharedConstants;

/**
 * Random tokens for refresh cookies and email links. Only the hash is stored, so a database leak does not
 * expose usable tokens.
 */
public final class OpaqueTokenHelper {

	private static final SecureRandom RANDOM = new SecureRandom();

	private OpaqueTokenHelper() {
	}

	/**
	 * A new URL-safe random token.
	 */
	public static String generate() {
		byte[] bytes = new byte[SharedConstants.OPAQUE_TOKEN_BYTES];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public static String hash(String token) {
		try {
			byte[] digest = MessageDigest.getInstance(SharedConstants.OPAQUE_TOKEN_HASH_ALGORITHM)
				.digest(token.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
