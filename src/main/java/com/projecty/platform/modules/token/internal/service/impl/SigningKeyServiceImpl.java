package com.projecty.platform.modules.token.internal.service.impl;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.projecty.platform.modules.token.internal.constants.TokenConstants;
import com.projecty.platform.modules.token.internal.entity.SigningKey;
import com.projecty.platform.modules.token.internal.repository.SigningKeyRepository;
import com.projecty.platform.modules.token.internal.service.SigningKeyService;

import org.springframework.stereotype.Service;

/**
 * On first startup, generates a key and stores it, so tokens stay valid across restarts. The newest active key
 * signs; all active keys are published for verification, which is what makes key rotation possible later.
 */
@Service
public class SigningKeyServiceImpl implements SigningKeyService {

	private final List<RSAKey> keys;

	public SigningKeyServiceImpl(SigningKeyRepository repository, Clock clock) {
		List<SigningKey> stored = repository.findByActiveTrueOrderByCreatedAtDesc();
		if (stored.isEmpty()) {
			stored = List.of(repository.save(generate(clock)));
		}
		this.keys = stored.stream().map(SigningKeyServiceImpl::toRsaKey).toList();
	}

	@Override
	public RSAKey signingKey() {
		return this.keys.getFirst();
	}

	@Override
	public JWKSet publicKeys() {
		return new JWKSet(this.keys.stream().<JWK>map(RSAKey::toPublicJWK).toList());
	}

	@Override
	public JWKSource<SecurityContext> jwkSource() {
		JWKSet keySet = new JWKSet(List.<JWK>copyOf(this.keys));
		return (selector, context) -> selector.select(keySet);
	}

	private static SigningKey generate(Clock clock) {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance(TokenConstants.KEY_ALGORITHM);
			generator.initialize(TokenConstants.RSA_KEY_SIZE);
			KeyPair pair = generator.generateKeyPair();
			Base64.Encoder base64 = Base64.getEncoder();
			return new SigningKey(UUID.randomUUID().toString(), base64.encodeToString(pair.getPublic().getEncoded()),
					base64.encodeToString(pair.getPrivate().getEncoded()), clock.instant());
		}
		catch (GeneralSecurityException ex) {
			throw new IllegalStateException("Could not generate a signing key", ex);
		}
	}

	private static RSAKey toRsaKey(SigningKey key) {
		try {
			KeyFactory factory = KeyFactory.getInstance(TokenConstants.KEY_ALGORITHM);
			Base64.Decoder base64 = Base64.getDecoder();
			RSAPublicKey publicKey = (RSAPublicKey) factory
				.generatePublic(new X509EncodedKeySpec(base64.decode(key.getPublicKey())));
			RSAPrivateKey privateKey = (RSAPrivateKey) factory
				.generatePrivate(new PKCS8EncodedKeySpec(base64.decode(key.getPrivateKey())));
			return new RSAKey.Builder(publicKey).privateKey(privateKey)
				.keyID(key.getKid())
				.keyUse(KeyUse.SIGNATURE)
				.algorithm(JWSAlgorithm.RS256)
				.build();
		}
		catch (GeneralSecurityException ex) {
			throw new IllegalStateException("Stored signing key " + key.getKid() + " is unreadable", ex);
		}
	}

}
