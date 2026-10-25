package com.projecty.platform.modules.token.internal.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * An RSA key pair used to sign access tokens, stored as Base64 DER (X.509 public, PKCS#8 private).
 *
 * The private key is not yet encrypted at rest; that comes with field-level encryption (SEC-BR-11).
 */
@Entity
@Table(name = "signing_key")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SigningKey {

	@Id
	private String kid;

	@Column(columnDefinition = "text")
	private String publicKey;

	@Column(columnDefinition = "text")
	private String privateKey;

	private boolean active;

	private Instant createdAt;

	public SigningKey(String kid, String publicKey, String privateKey, Instant now) {
		this.kid = kid;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.active = true;
		this.createdAt = now;
	}

}
