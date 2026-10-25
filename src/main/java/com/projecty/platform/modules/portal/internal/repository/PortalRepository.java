package com.projecty.platform.modules.portal.internal.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.modules.portal.api.dto.PortalSummaryResponseDTO;
import com.projecty.platform.modules.portal.internal.entity.Portal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PortalRepository extends JpaRepository<Portal, UUID> {

	boolean existsBySlug(String slug);

	@Query("select p.slug from Portal p where p.slug in :slugs")
	List<String> findExistingSlugs(Collection<String> slugs);

	@Query("""
			select new com.projecty.platform.modules.portal.api.dto.PortalSummaryResponseDTO(
				p.id, p.name, p.slug, p.ownerUserId, p.createdAt,
				(select count(m) from PortalMember m where m.portalId = p.id),
				(select count(a) from PortalMember a where a.portalId = p.id
					and a.status = com.projecty.platform.modules.portal.internal.entity.MemberStatus.ACTIVE))
			from Portal p
			order by p.createdAt desc""")
	List<PortalSummaryResponseDTO> findAllSummaries();

	@Query("""
			select new com.projecty.platform.modules.portal.api.dto.PortalSummaryResponseDTO(
				p.id, p.name, p.slug, p.ownerUserId, p.createdAt,
				(select count(m) from PortalMember m where m.portalId = p.id),
				(select count(a) from PortalMember a where a.portalId = p.id
					and a.status = com.projecty.platform.modules.portal.internal.entity.MemberStatus.ACTIVE))
			from Portal p
			where p.id = :portalId""")
	Optional<PortalSummaryResponseDTO> findSummary(UUID portalId);

}
