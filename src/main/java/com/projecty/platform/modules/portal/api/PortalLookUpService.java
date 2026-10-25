package com.projecty.platform.modules.portal.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.modules.portal.api.dto.PortalMemberResponseDTO;
import com.projecty.platform.modules.portal.api.dto.PortalMembershipResponseDTO;
import com.projecty.platform.modules.portal.api.dto.PortalSummaryResponseDTO;

public interface PortalLookUpService {

	/**
	 * Ids of the portals the user is an active member of, oldest membership first.
	 */
	List<UUID> findActivePortalIds(UUID userId);

	/**
	 * The user's active portals with their profile in each, sorted by portal name.
	 */
	List<PortalMembershipResponseDTO> findMemberships(UUID userId);

	/**
	 * Every portal with its member counts, newest first (admin console).
	 */
	List<PortalSummaryResponseDTO> findAllPortals();

	Optional<PortalSummaryResponseDTO> findPortal(UUID portalId);

	/**
	 * Everyone in the portal, in any status, oldest member first.
	 */
	List<PortalMemberResponseDTO> findMembers(UUID portalId);

	boolean isActiveMember(UUID portalId, UUID userId);

}
