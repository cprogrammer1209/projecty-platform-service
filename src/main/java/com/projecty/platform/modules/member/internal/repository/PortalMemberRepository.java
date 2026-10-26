package com.projecty.platform.modules.member.internal.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.modules.member.internal.entity.MemberStatus;
import com.projecty.platform.modules.member.internal.entity.PortalMember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PortalMemberRepository extends JpaRepository<PortalMember, PortalMember.Key> {

	@Query("""
			select m.portalId from PortalMember m
			where m.userId = :userId
			and m.status = com.projecty.platform.modules.member.internal.entity.MemberStatus.ACTIVE
			order by m.joinedAt""")
	List<UUID> findActivePortalIds(UUID userId);

	List<PortalMember> findByUserIdAndStatus(UUID userId, MemberStatus status);

	List<PortalMember> findByPortalIdOrderByJoinedAt(UUID portalId);

	List<PortalMember> findByPortalIdIn(Collection<UUID> portalIds);

	Optional<PortalMember> findByPortalIdAndUserId(UUID portalId, UUID userId);

	boolean existsByPortalIdAndUserIdAndStatus(UUID portalId, UUID userId, MemberStatus status);

	long countByPortalIdAndProfileIdAndStatus(UUID portalId, UUID profileId, MemberStatus status);

}
