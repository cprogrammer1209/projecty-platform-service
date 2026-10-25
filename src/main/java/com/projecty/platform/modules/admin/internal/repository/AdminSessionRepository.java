package com.projecty.platform.modules.admin.internal.repository;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import com.projecty.platform.modules.admin.internal.entity.AdminSession;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface AdminSessionRepository extends JpaRepository<AdminSession, UUID> {

	/**
	 * Locks the row so two concurrent refreshes with the same token can't both succeed.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<AdminSession> findByRefreshTokenHash(String refreshTokenHash);

}
