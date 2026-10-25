package com.projecty.platform.modules.account.internal.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

/**
 * A single-use token sent by email (SEC-BR-04).
 */
@Entity
@Table(name = "email_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailToken {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	private UUID id;

	private UUID userId;

	@Enumerated(EnumType.STRING)
	private EmailTokenPurpose purpose;

	private String tokenHash;

	private Instant expiresAt;

	private Instant usedAt;

	private Instant createdAt;

	public EmailToken(UUID userId, EmailTokenPurpose purpose, String tokenHash, Instant now, Instant expiresAt) {
		this.userId = userId;
		this.purpose = purpose;
		this.tokenHash = tokenHash;
		this.createdAt = now;
		this.expiresAt = expiresAt;
	}

	public boolean isUsable(Instant now) {
		return this.usedAt == null && now.isBefore(this.expiresAt);
	}

	public void markUsed(Instant now) {
		this.usedAt = now;
	}

}
