package com.projecty.platform.modules.session.internal.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.projecty.platform.modules.session.internal.entity.AuthSession;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {

	@Query("select s.id from AuthSession s where s.userId = :userId and s.revokedAt is null")
	List<UUID> findActiveIdsByUserId(UUID userId);

	@Modifying
	@Query("update AuthSession s set s.revokedAt = :now where s.userId = :userId and s.revokedAt is null")
	int revokeAllForUser(UUID userId, Instant now);

}
