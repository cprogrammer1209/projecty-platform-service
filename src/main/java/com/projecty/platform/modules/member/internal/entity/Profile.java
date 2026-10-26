package com.projecty.platform.modules.member.internal.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

/**
 * A set of permissions within one portal. System profiles (Admin, Manager, Employee, Client) are seeded per
 * portal and can't be deleted (USR-BR-07).
 */
@Entity
@Table(name = "profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	private UUID id;

	private UUID portalId;

	private String profileKey;

	private String name;

	private String description;

	private boolean systemProfile;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "profile_permission", joinColumns = @JoinColumn(name = "profile_id"))
	@Column(name = "permission")
	private Set<String> permissions = new HashSet<>();

	@Column(updatable = false)
	private Instant createdAt;

	public Profile(UUID portalId, String profileKey, String name, String description, Set<String> permissions,
			Instant now) {
		this.portalId = portalId;
		this.profileKey = profileKey;
		this.name = name;
		this.description = description;
		this.systemProfile = true;
		this.permissions = new HashSet<>(permissions);
		this.createdAt = now;
	}

}
