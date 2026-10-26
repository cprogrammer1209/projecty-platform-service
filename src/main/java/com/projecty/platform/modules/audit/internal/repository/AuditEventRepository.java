package com.projecty.platform.modules.audit.internal.repository;

import java.util.List;
import java.util.UUID;

import com.projecty.platform.modules.audit.internal.entity.AuditEvent;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID>, JpaSpecificationExecutor<AuditEvent> {

	List<AuditEvent> findByOrderByOccurredAtDesc(Pageable page);

	List<AuditEvent> findByPortalIdOrderByOccurredAtDesc(UUID portalId, Pageable page);

}
