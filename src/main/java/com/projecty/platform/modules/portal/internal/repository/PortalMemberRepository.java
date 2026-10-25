package com.projecty.platform.modules.portal.internal.repository;

import java.util.List;
import java.util.UUID;

import com.projecty.platform.modules.portal.api.dto.PortalMembershipResponseDTO;
import com.projecty.platform.modules.portal.internal.entity.PortalMember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PortalMemberRepository extends JpaRepository<PortalMember, PortalMember.Key> {

	@Query("""
			select m.portalId from PortalMember m
			where m.userId = :userId
			and m.status = com.projecty.platform.modules.portal.internal.entity.MemberStatus.ACTIVE
			order by m.joinedAt""")
	List<UUID> findActivePortalIds(UUID userId);

	@Query("""
			select new com.projecty.platform.modules.portal.api.dto.PortalMembershipResponseDTO(
				p.id, p.name, p.slug, cast(m.profile as string))
			from PortalMember m join Portal p on p.id = m.portalId
			where m.userId = :userId
			and m.status = com.projecty.platform.modules.portal.internal.entity.MemberStatus.ACTIVE
			order by p.name""")
	List<PortalMembershipResponseDTO> findMemberships(UUID userId);

}
