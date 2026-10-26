package com.projecty.platform.modules.audit.internal.controller;

import java.util.List;
import java.util.UUID;

import com.projecty.platform.common.dto.AppResponse;
import com.projecty.platform.common.dto.PagedResponse;
import com.projecty.platform.common.security.annotation.RequirePermission;
import com.projecty.platform.modules.audit.internal.constants.AuditConstants;
import com.projecty.platform.modules.audit.internal.dto.AuditEventResponseDTO;
import com.projecty.platform.modules.audit.internal.service.AuditEventService;

import com.projecty.platform.modules.shared.constants.PermissionConstants;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Audit logs: the platform-wide list for the admin console (platform admins only, see SecurityConfig) and the
 * current portal's own log for its Admins.
 */
@RestController
public class AuditEventController {

	private final AuditEventService auditEventService;

	public AuditEventController(AuditEventService auditEventService) {
		this.auditEventService = auditEventService;
	}

	@GetMapping("/api/audit-events")
	@RequirePermission(PermissionConstants.AUDIT_VIEW)
	public AppResponse<PagedResponse<AuditEventResponseDTO>> listForPortal(
			@RequestParam(required = false) String category, @RequestParam(required = false) String action,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "" + AuditConstants.DEFAULT_PAGE_SIZE) int size) {
		return AppResponse.ok(this.auditEventService.findForCurrentPortal(category, action, page, size));
	}

	@GetMapping("/api/admin/audit-events")
	public AppResponse<List<AuditEventResponseDTO>> list(@RequestParam(required = false) UUID portalId) {
		return AppResponse.ok(this.auditEventService.findRecent(portalId));
	}

}
