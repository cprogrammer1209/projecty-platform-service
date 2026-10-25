package com.projecty.platform.modules.session.internal.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

/**
 * One refresh token in a session's rotation chain. Each token can be used once; presenting a used token again
 * means it was copied, so the whole session is revoked.
 */
@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	private UUID id;

	private UUID sessionId;

	private String tokenHash;

	private Instant createdAt;

	private Instant usedAt;

	public RefreshToken(UUID sessionId, String tokenHash, Instant now) {
		this.sessionId = sessionId;
		this.tokenHash = tokenHash;
		this.createdAt = now;
	}

	public boolean isUsed() {
		return this.usedAt != null;
	}

	public void markUsed(Instant now) {
		this.usedAt = now;
	}

}
