package com.projecty.platform.modules.token.internal.service;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

/**
 * Holds the RSA keys that sign access tokens.
 */
public interface SigningKeyService {

	/**
	 * The key used to sign new tokens.
	 */
	RSAKey signingKey();

	/**
	 * Public halves of all active keys, as served at {@code /.well-known/jwks.json}.
	 */
	JWKSet publicKeys();

	/**
	 * All active keys (with private parts) for the JWT encoder and decoder.
	 */
	JWKSource<SecurityContext> jwkSource();

}
