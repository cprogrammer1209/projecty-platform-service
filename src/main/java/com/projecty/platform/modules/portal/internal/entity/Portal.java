package com.projecty.platform.modules.portal.internal.entity;

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
 * A tenant: an organization's isolated workspace.
 */
@Entity
@Table(name = "portal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Portal {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	private UUID id;

	private String name;

	private String slug;

	private UUID ownerUserId;

	@Column(updatable = false)
	private Instant createdAt;

	private Instant updatedAt;

	public Portal(String name, String slug, UUID ownerUserId, Instant now) {
		this.name = name;
		this.slug = slug;
		this.ownerUserId = ownerUserId;
		this.createdAt = now;
		this.updatedAt = now;
	}

}
