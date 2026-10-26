package com.projecty.platform.modules.member.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import com.projecty.platform.modules.member.internal.entity.Invitation;
import com.projecty.platform.modules.member.internal.entity.InvitationStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

	/**
	 * Locks the row so an invitation can't be accepted twice concurrently.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Invitation> findByTokenHash(String tokenHash);

	List<Invitation> findByPortalIdAndStatusOrderByCreatedAtDesc(UUID portalId, InvitationStatus status);

	Optional<Invitation> findByIdAndPortalId(UUID id, UUID portalId);

	@Query("select i from Invitation i where i.portalId = :portalId and lower(i.email) = lower(:email) and i.status = :status")
	List<Invitation> findOpen(UUID portalId, String email, InvitationStatus status);

}
