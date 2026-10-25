package com.projecty.platform.modules.account.internal.entity;

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
 * A person's identity. One identity can belong to many portals (see {@code PortalMember}).
 */
@Entity
@Table(name = "app_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppUser {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	private UUID id;

	private String email;

	private String fullName;

	private String passwordHash;

	private Instant emailVerifiedAt;

	@Enumerated(EnumType.STRING)
	private UserStatus status;

	private int failedLoginCount;

	private Instant firstFailedLoginAt;

	private Instant lockedUntil;

	private Instant lastLoginAt;

	/**
	 * Personal time zone (PTL-US-12); null means "use the portal's".
	 */
	private String timeZone;

	@Column(updatable = false)
	private Instant createdAt;

	private Instant updatedAt;

	public AppUser(String email, String fullName, String passwordHash, Instant now) {
		this.email = email;
		this.fullName = fullName;
		this.passwordHash = passwordHash;
		this.status = UserStatus.ACTIVE;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public boolean isEmailVerified() {
		return this.emailVerifiedAt != null;
	}

	public boolean isActive() {
		return this.status == UserStatus.ACTIVE;
	}

	public boolean isLocked(Instant now) {
		return this.lockedUntil != null && now.isBefore(this.lockedUntil);
	}

	public void markEmailVerified(Instant now) {
		if (this.emailVerifiedAt == null) {
			this.emailVerifiedAt = now;
			this.updatedAt = now;
		}
	}

	/**
	 * Counts a failed login. {@code maxAttempts} failures within {@code window} lock the identity for
	 * {@code lockDuration} (SEC-BR-02).
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

	public void updateTimeZone(String timeZone, Instant now) {
		this.timeZone = timeZone;
		this.updatedAt = now;
	}

	public void recordSuccessfulLogin(Instant now) {
		this.failedLoginCount = 0;
		this.firstFailedLoginAt = null;
		this.lockedUntil = null;
		this.lastLoginAt = now;
		this.updatedAt = now;
	}

	/**
	 * Sets a new password after a reset. The reset link proved control of the email address, so it also counts
	 * as verification, and it clears any lockout.
	 */
	public void resetPassword(String passwordHash, Instant now) {
		this.passwordHash = passwordHash;
		this.failedLoginCount = 0;
		this.firstFailedLoginAt = null;
		this.lockedUntil = null;
		markEmailVerified(now);
		this.updatedAt = now;
	}

}
