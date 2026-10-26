package com.projecty.platform.modules.portal.internal.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.projecty.platform.modules.portal.internal.entity.Portal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PortalRepository extends JpaRepository<Portal, UUID> {

	boolean existsBySlug(String slug);

	@Query("select p.slug from Portal p where p.slug in :slugs")
	List<String> findExistingSlugs(Collection<String> slugs);

	List<Portal> findAllByOrderByCreatedAtDesc();

}
