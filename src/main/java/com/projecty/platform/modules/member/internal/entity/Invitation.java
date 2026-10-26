package com.projecty.platform.modules.member.internal.entity;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
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
 * An emailed invitation to join a portal with a profile (USR-US-01). Valid once, for 7 days (USR-BR-05).
 */
@Entity
@Table(name = "invitation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invitation {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	private UUID id;

	private UUID portalId;

	private String email;

	private UUID profileId;

	private UUID invitedByUserId;

	private String tokenHash;

	@Enumerated(EnumType.STRING)
	private InvitationStatus status;

	private Instant expiresAt;

	@Column(updatable = false)
	private Instant createdAt;

	private Instant acceptedAt;

	private UUID acceptedUserId;

	public Invitation(UUID portalId, String email, UUID profileId, UUID invitedByUserId, String tokenHash,
			Instant now, Duration ttl) {
		this.portalId = portalId;
		this.email = email;
		this.profileId = profileId;
		this.invitedByUserId = invitedByUserId;
		this.tokenHash = tokenHash;
		this.status = InvitationStatus.PENDING;
		this.createdAt = now;
		this.expiresAt = now.plus(ttl);
	}

	public boolean isUsable(Instant now) {
		return this.status == InvitationStatus.PENDING && now.isBefore(this.expiresAt);
	}

	public void accept(UUID userId, Instant now) {
		this.status = InvitationStatus.ACCEPTED;
		this.acceptedUserId = userId;
		this.acceptedAt = now;
	}

	public void revoke() {
		this.status = InvitationStatus.REVOKED;
	}

}
