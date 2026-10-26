package com.projecty.platform.modules.audit.internal.service;

import java.util.List;
import java.util.UUID;

import com.projecty.platform.common.dto.PagedResponse;
import com.projecty.platform.modules.audit.api.AuditWriteService;
import com.projecty.platform.modules.audit.internal.dto.AuditEventResponseDTO;

public interface AuditEventService extends AuditWriteService {

	/**
	 * Newest events first, across all tenants or for one tenant.
	 */
	List<AuditEventResponseDTO> findRecent(UUID portalId);

	/**
	 * The current portal's events, newest first (the portal comes from {@code RequestPortalContext}).
	 * @param category optional filter
	 * @param action optional filter
	 */
	PagedResponse<AuditEventResponseDTO> findForCurrentPortal(String category, String action, int page, int size);

}
