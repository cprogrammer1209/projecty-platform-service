package com.projecty.platform.modules.portal.internal.service;

import java.util.UUID;

import com.projecty.platform.modules.portal.api.PortalLookUpService;
import com.projecty.platform.modules.portal.internal.dto.PortalCreateDTO;
import com.projecty.platform.modules.portal.internal.dto.PortalResponseDTO;
import com.projecty.platform.modules.portal.internal.dto.SlugAvailabilityResponseDTO;

public interface PortalService extends PortalLookUpService {

	/**
	 * Creates a portal with the caller as its Owner (PTL-US-01, PTL-BR-03).
	 */
	PortalResponseDTO create(UUID userId, PortalCreateDTO request);

	SlugAvailabilityResponseDTO checkSlug(String slug);

}
