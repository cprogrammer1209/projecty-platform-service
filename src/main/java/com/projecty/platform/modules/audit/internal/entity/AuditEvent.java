package com.projecty.platform.modules.audit.internal.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.UuidGenerator;

/**
 * An audited action. Rows are never updated or deleted.
 */
@Entity
@Table(name = "audit_event")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditEvent {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	private UUID id;

	private Instant occurredAt;

	private String category;

	private String action;

	private String actorType;

	private UUID actorId;

	private String actorName;

	private UUID portalId;

	private UUID targetUserId;

	private String targetName;

	private String detail;

	public AuditEvent(Instant occurredAt, String category, String action, String actorType, UUID actorId,
			String actorName, UUID portalId, UUID targetUserId, String targetName, String detail) {
		this.occurredAt = occurredAt;
		this.category = category;
		this.action = action;
		this.actorType = actorType;
		this.actorId = actorId;
		this.actorName = actorName;
		this.portalId = portalId;
		this.targetUserId = targetUserId;
		this.targetName = targetName;
		this.detail = detail;
	}

}
