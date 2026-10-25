package com.projecty.platform.modules.impersonation.internal.entity;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

/**
 * A platform admin's request to act as a customer user. The launch code works once, briefly; the support
 * session it opens ends at {@code expiresAt} or when ended.
 */
@Entity
@Table(name = "impersonation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Impersonation {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	private UUID id;

	private UUID adminUserId;

	private UUID portalId;

	private UUID targetUserId;

	private String reason;

	private String comment;

	private int durationMinutes;

	private String codeHash;

	private Instant codeExpiresAt;

	private Instant redeemedAt;

	private Instant expiresAt;

	private Instant endedAt;

	@Column(updatable = false)
	private Instant createdAt;

	public Impersonation(UUID adminUserId, UUID portalId, UUID targetUserId, String reason, String comment,
			int durationMinutes, String codeHash, Instant now, Duration codeTtl) {
		this.adminUserId = adminUserId;
		this.portalId = portalId;
		this.targetUserId = targetUserId;
		this.reason = reason;
		this.comment = comment;
		this.durationMinutes = durationMinutes;
		this.codeHash = codeHash;
		this.codeExpiresAt = now.plus(codeTtl);
		this.expiresAt = now.plus(Duration.ofMinutes(durationMinutes));
		this.createdAt = now;
	}

	public boolean canRedeem(Instant now) {
		return this.redeemedAt == null && this.endedAt == null && now.isBefore(this.codeExpiresAt)
				&& now.isBefore(this.expiresAt);
	}

	public void redeem(Instant now) {
		this.redeemedAt = now;
	}

	/**
	 * @return false if it had already ended
	 */
	public boolean end(Instant now) {
		if (this.endedAt != null) {
			return false;
		}
		this.endedAt = now;
		return true;
	}

}
