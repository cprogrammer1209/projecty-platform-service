package com.projecty.platform.modules.session.internal.entity;

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
 * A login on one device. It stays alive while it is used at least once per idle timeout, up to an absolute
 * limit (SEC-BR-03).
 */
@Entity
@Table(name = "auth_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthSession {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	private UUID id;

	private UUID userId;

	private String userAgent;

	private String ip;

	private Instant createdAt;

	private Instant lastSeenAt;

	private Instant idleExpiresAt;

	private Instant absoluteExpiresAt;

	private Instant revokedAt;

	/**
	 * Set when a platform admin opened this session to act as the user (support impersonation).
	 */
	private UUID impersonationId;

	public AuthSession(UUID userId, String userAgent, String ip, Instant now, Duration idleTimeout,
			Duration absoluteTimeout) {
		this.userId = userId;
		this.userAgent = userAgent;
		this.ip = ip;
		this.createdAt = now;
		this.absoluteExpiresAt = now.plus(absoluteTimeout);
		touch(now, idleTimeout);
	}

	/**
	 * A support session: it never outlives the impersonation's end time.
	 */
	public static AuthSession impersonated(UUID userId, UUID impersonationId, Instant impersonationExpiresAt,
			String userAgent, String ip, Instant now, Duration idleTimeout) {
		AuthSession session = new AuthSession(userId, userAgent, ip, now, idleTimeout,
				Duration.between(now, impersonationExpiresAt));
		session.impersonationId = impersonationId;
		return session;
	}

	public boolean isImpersonated() {
		return this.impersonationId != null;
	}

	public boolean isUsable(Instant now) {
		return this.revokedAt == null && now.isBefore(expiresAt());
	}

	/**
	 * When the session ends if it is not used again.
	 */
	public Instant expiresAt() {
		return this.idleExpiresAt.isBefore(this.absoluteExpiresAt) ? this.idleExpiresAt : this.absoluteExpiresAt;
	}

	public void touch(Instant now, Duration idleTimeout) {
		this.lastSeenAt = now;
		this.idleExpiresAt = now.plus(idleTimeout);
	}

	public void revoke(Instant now) {
		if (this.revokedAt == null) {
			this.revokedAt = now;
		}
	}

}
