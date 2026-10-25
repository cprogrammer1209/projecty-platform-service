package com.projecty.platform.modules.portal.api;

import java.util.List;
import java.util.UUID;

import com.projecty.platform.modules.portal.api.dto.PortalMembershipResponseDTO;

public interface PortalLookUpService {

	/**
	 * Ids of the portals the user is an active member of, oldest membership first.
	 */
	List<UUID> findActivePortalIds(UUID userId);

	/**
	 * The user's active portals with their profile in each, sorted by portal name.
	 */
	List<PortalMembershipResponseDTO> findMemberships(UUID userId);

}
