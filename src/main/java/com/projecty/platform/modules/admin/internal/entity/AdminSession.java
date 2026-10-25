package com.projecty.platform.modules.admin.internal.entity;

import java.time.Duration;
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
 * An admin console login. Each refresh replaces the stored hash, so an old refresh token stops working.
 */
@Entity
@Table(name = "admin_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminSession {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	private UUID id;

	private UUID adminUserId;

	private String refreshTokenHash;

	private String userAgent;

	private String ip;

	private Instant createdAt;

	private Instant lastSeenAt;

	private Instant idleExpiresAt;

	private Instant absoluteExpiresAt;

	private Instant revokedAt;

	public AdminSession(UUID adminUserId, String refreshTokenHash, String userAgent, String ip, Instant now,
			Duration idleTimeout, Duration absoluteTimeout) {
		this.adminUserId = adminUserId;
		this.refreshTokenHash = refreshTokenHash;
		this.userAgent = userAgent;
		this.ip = ip;
		this.createdAt = now;
		this.absoluteExpiresAt = now.plus(absoluteTimeout);
		this.lastSeenAt = now;
		this.idleExpiresAt = now.plus(idleTimeout);
	}

	public boolean isUsable(Instant now) {
		return this.revokedAt == null && now.isBefore(expiresAt());
	}

	public Instant expiresAt() {
		return this.idleExpiresAt.isBefore(this.absoluteExpiresAt) ? this.idleExpiresAt : this.absoluteExpiresAt;
	}

	public void rotate(String refreshTokenHash, Instant now, Duration idleTimeout) {
		this.refreshTokenHash = refreshTokenHash;
		this.lastSeenAt = now;
		this.idleExpiresAt = now.plus(idleTimeout);
	}

	public void revoke(Instant now) {
		if (this.revokedAt == null) {
			this.revokedAt = now;
		}
	}

}
