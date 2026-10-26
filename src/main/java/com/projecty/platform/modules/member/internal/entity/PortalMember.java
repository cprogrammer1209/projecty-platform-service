package com.projecty.platform.modules.member.internal.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A person inside a portal, with one profile there. The same identity can have a different profile in each
 * portal (USR-BR-11: one membership per identity per portal).
 */
@Entity
@Table(name = "portal_member")
@IdClass(PortalMember.Key.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortalMember {

	@Id
	private UUID portalId;

	@Id
	private UUID userId;

	private UUID profileId;

	@Enumerated(EnumType.STRING)
	private MemberStatus status;

	private Instant joinedAt;

	private Instant deactivatedAt;

	public PortalMember(UUID portalId, UUID userId, UUID profileId, Instant now) {
		this.portalId = portalId;
		this.userId = userId;
		this.profileId = profileId;
		this.status = MemberStatus.ACTIVE;
		this.joinedAt = now;
	}

	public boolean isActive() {
		return this.status == MemberStatus.ACTIVE;
	}

	public void changeProfile(UUID profileId) {
		this.profileId = profileId;
	}

	/**
	 * Access ends at once (USR-BR-09); history stays attributed to them.
	 */
	public void deactivate(Instant now) {
		this.status = MemberStatus.DEACTIVATED;
		this.deactivatedAt = now;
	}

	public void reactivate() {
		this.status = MemberStatus.ACTIVE;
		this.deactivatedAt = null;
	}

	public record Key(UUID portalId, UUID userId) implements Serializable {
	}

}
