package com.projecty.platform.modules.audit.internal.service;

import java.util.List;
import java.util.UUID;

import com.projecty.platform.modules.audit.api.AuditWriteService;
import com.projecty.platform.modules.audit.internal.dto.AuditEventResponseDTO;

public interface AuditEventService extends AuditWriteService {

	/**
	 * Newest events first, across all tenants or for one tenant.
	 */
	List<AuditEventResponseDTO> findRecent(UUID portalId);

}
