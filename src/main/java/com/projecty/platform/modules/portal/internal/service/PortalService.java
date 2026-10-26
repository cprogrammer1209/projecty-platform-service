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
	 * Creates a portal owned by the caller (PTL-US-01, PTL-BR-03). Publishes {@code PortalCreatedEvent}, on
	 * which the member module adds the caller as Admin (and enforces the portal limit, PTL-BR-10). Audited and
	 * published to the outbox as {@code PortalCreated}.
	 * @param userName the caller's name, for the audit log
	 */
	PortalResponseDTO create(UUID userId, String userName, PortalCreateDTO request);

	SlugAvailabilityResponseDTO checkSlug(String slug);

	/**
	 * The portal of the current request ({@code RequestPortalContext}).
	 */
	CurrentPortalResponseDTO getCurrent();

	/**
	 * Changes the current portal's display settings (PTL-US-02); audited (PTL-BR-04). The permission is
	 * checked by {@code @RequirePermission} on the endpoint.
	 * @param userName the caller's name, for the audit log
	 */
	CurrentPortalResponseDTO updateSettings(UUID userId, String userName, PortalSettingsUpdateDTO request);

}
