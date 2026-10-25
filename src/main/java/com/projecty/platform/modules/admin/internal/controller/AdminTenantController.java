package com.projecty.platform.modules.admin.internal.controller;

import java.util.List;
import java.util.UUID;

import com.projecty.platform.common.dto.AppResponse;
import com.projecty.platform.modules.admin.internal.dto.TenantResponseDTO;
import com.projecty.platform.modules.admin.internal.dto.TenantUserResponseDTO;
import com.projecty.platform.modules.admin.internal.service.AdminTenantService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tenants (portals) and their users, for platform admins only.
 */
@RestController
@RequestMapping("/api/admin/tenants")
public class AdminTenantController {

	private final AdminTenantService adminTenantService;

	public AdminTenantController(AdminTenantService adminTenantService) {
		this.adminTenantService = adminTenantService;
	}

	@GetMapping
	public AppResponse<List<TenantResponseDTO>> list() {
		return AppResponse.ok(this.adminTenantService.listTenants());
	}

	@GetMapping("/{tenantId}")
	public AppResponse<TenantResponseDTO> get(@PathVariable UUID tenantId) {
		return AppResponse.ok(this.adminTenantService.getTenant(tenantId));
	}

	@GetMapping("/{tenantId}/users")
	public AppResponse<List<TenantUserResponseDTO>> users(@PathVariable UUID tenantId) {
		return AppResponse.ok(this.adminTenantService.listUsers(tenantId));
	}

}
