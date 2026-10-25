package com.projecty.platform.modules.portal.internal.entity;

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
 * A person inside a portal. The same identity can have a different profile in each portal.
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

	@Enumerated(EnumType.STRING)
	private PortalProfile profile;

	@Enumerated(EnumType.STRING)
	private MemberStatus status;

	private Instant joinedAt;

	public PortalMember(UUID portalId, UUID userId, PortalProfile profile, Instant now) {
		this.portalId = portalId;
		this.userId = userId;
		this.profile = profile;
		this.status = MemberStatus.ACTIVE;
		this.joinedAt = now;
	}

	public record Key(UUID portalId, UUID userId) implements Serializable {
	}

}
