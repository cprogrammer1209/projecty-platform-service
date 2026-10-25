package com.projecty.platform.modules.portal.internal.service;

import java.util.UUID;

import com.projecty.platform.modules.portal.api.PortalLookUpService;
import com.projecty.platform.modules.portal.internal.dto.CurrentPortalResponseDTO;
import com.projecty.platform.modules.portal.internal.dto.PortalCreateDTO;
import com.projecty.platform.modules.portal.internal.dto.PortalResponseDTO;
import com.projecty.platform.modules.portal.internal.dto.PortalSettingsUpdateDTO;
import com.projecty.platform.modules.portal.internal.dto.SlugAvailabilityResponseDTO;

public interface PortalService extends PortalLookUpService {

	/**
	 * Creates a portal with the caller as its Owner (PTL-US-01, PTL-BR-03).
	 */
	PortalResponseDTO create(UUID userId, PortalCreateDTO request);

	SlugAvailabilityResponseDTO checkSlug(String slug);

	/**
	 * The portal of the current request ({@code RequestPortalContext}) as the caller sees it.
	 */
	CurrentPortalResponseDTO getCurrent(UUID userId);

	/**
	 * Changes the current portal's display settings (PTL-US-02). Owner only for now; audited (PTL-BR-04).
	 * @param userName the caller's name, for the audit log
	 */
	CurrentPortalResponseDTO updateSettings(UUID userId, String userName, PortalSettingsUpdateDTO request);

}
