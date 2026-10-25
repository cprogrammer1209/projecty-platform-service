package com.projecty.platform.modules.account.internal.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.modules.account.internal.entity.EmailToken;
import com.projecty.platform.modules.account.internal.entity.EmailTokenPurpose;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface EmailTokenRepository extends JpaRepository<EmailToken, UUID> {

	Optional<EmailToken> findByTokenHashAndPurpose(String tokenHash, EmailTokenPurpose purpose);

	@Modifying
	@Query("""
			update EmailToken t set t.usedAt = :now
			where t.userId = :userId and t.purpose = :purpose and t.usedAt is null""")
	int invalidateUnused(UUID userId, EmailTokenPurpose purpose, Instant now);

}
