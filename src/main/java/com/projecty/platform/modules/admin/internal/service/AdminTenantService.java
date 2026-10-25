package com.projecty.platform.modules.admin.internal.service;

import java.util.List;
import java.util.UUID;

import com.projecty.platform.modules.admin.internal.dto.TenantResponseDTO;
import com.projecty.platform.modules.admin.internal.dto.TenantUserResponseDTO;

/**
 * Read-only tenant views for the admin console.
 */
public interface AdminTenantService {

	List<TenantResponseDTO> listTenants();

	TenantResponseDTO getTenant(UUID tenantId);

	List<TenantUserResponseDTO> listUsers(UUID tenantId);

}
