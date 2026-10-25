package com.projecty.platform.modules.session.internal.repository;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import com.projecty.platform.modules.session.internal.entity.RefreshToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

	/**
	 * Locks the row so two concurrent refreshes with the same token can't both succeed.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<RefreshToken> findByTokenHash(String tokenHash);

}
