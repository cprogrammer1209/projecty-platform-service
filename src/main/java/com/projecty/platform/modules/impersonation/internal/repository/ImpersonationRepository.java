package com.projecty.platform.modules.impersonation.internal.repository;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import com.projecty.platform.modules.impersonation.internal.entity.Impersonation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface ImpersonationRepository extends JpaRepository<Impersonation, UUID> {

	/**
	 * Locks the row so a launch code can't be redeemed twice concurrently.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Impersonation> findByCodeHash(String codeHash);

}
