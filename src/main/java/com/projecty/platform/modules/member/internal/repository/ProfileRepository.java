package com.projecty.platform.modules.member.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.modules.member.internal.entity.Profile;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

	List<Profile> findByPortalIdOrderByCreatedAtAscNameAsc(UUID portalId);

	Optional<Profile> findByIdAndPortalId(UUID id, UUID portalId);

	Optional<Profile> findByPortalIdAndProfileKey(UUID portalId, String profileKey);

}
