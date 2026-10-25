package com.projecty.platform.modules.audit.internal.controller;

import java.util.List;
import java.util.UUID;

import com.projecty.platform.common.dto.AppResponse;
import com.projecty.platform.modules.audit.internal.dto.AuditEventResponseDTO;
import com.projecty.platform.modules.audit.internal.service.AuditEventService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Audit logs for the admin console (platform admins only, see SecurityConfig).
 */
@RestController
public class AuditEventController {

	private final AuditEventService auditEventService;

	public AuditEventController(AuditEventService auditEventService) {
		this.auditEventService = auditEventService;
	}

	@GetMapping("/api/admin/audit-events")
	public AppResponse<List<AuditEventResponseDTO>> list(@RequestParam(required = false) UUID portalId) {
		return AppResponse.ok(this.auditEventService.findRecent(portalId));
	}

}
