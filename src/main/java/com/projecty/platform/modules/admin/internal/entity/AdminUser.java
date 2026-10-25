package com.projecty.platform.modules.admin.internal.entity;

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
 * A platform administrator. Separate from customer identities ({@code AppUser}): an admin account can't sign
 * in to the customer app, and a customer can't sign in to the admin console.
 */
@Entity
@Table(name = "admin_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminUser {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	private UUID id;

	private String email;

	private String fullName;

	private String passwordHash;

	@Enumerated(EnumType.STRING)
	private AdminUserStatus status;

	private int failedLoginCount;

	private Instant firstFailedLoginAt;

	private Instant lockedUntil;

	private Instant lastLoginAt;

	@Column(updatable = false)
	private Instant createdAt;

	private Instant updatedAt;

	public AdminUser(String email, String fullName, String passwordHash, Instant now) {
		this.email = email;
		this.fullName = fullName;
		this.passwordHash = passwordHash;
		this.status = AdminUserStatus.ACTIVE;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public boolean isActive() {
		return this.status == AdminUserStatus.ACTIVE;
	}

	public boolean isLocked(Instant now) {
		return this.lockedUntil != null && now.isBefore(this.lockedUntil);
	}

	/**
	 * Counts a failed login; {@code maxAttempts} failures within {@code window} lock the account for
	 * {@code lockDuration}.
	 */
	public void recordFailedLogin(Instant now, int maxAttempts, Duration window, Duration lockDuration) {
		if (this.firstFailedLoginAt == null || now.isAfter(this.firstFailedLoginAt.plus(window))) {
			this.firstFailedLoginAt = now;
			this.failedLoginCount = 0;
		}
		this.failedLoginCount++;
		if (this.failedLoginCount >= maxAttempts) {
			this.lockedUntil = now.plus(lockDuration);
			this.failedLoginCount = 0;
			this.firstFailedLoginAt = null;
		}
		this.updatedAt = now;
	}

	public void recordSuccessfulLogin(Instant now) {
		this.failedLoginCount = 0;
		this.firstFailedLoginAt = null;
		this.lockedUntil = null;
		this.lastLoginAt = now;
		this.updatedAt = now;
	}

}
