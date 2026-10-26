package com.projecty.platform.modules.portal.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.modules.portal.api.dto.PortalSummaryResponseDTO;

/**
 * Portals themselves. Who belongs to a portal is the member module's ({@code MemberLookUpService}).
 */
public interface PortalLookUpService {

	/**
	 * Every portal, newest first (admin console).
	 */
	List<PortalSummaryResponseDTO> findAllPortals();

	Optional<PortalSummaryResponseDTO> findPortal(UUID portalId);

	List<PortalSummaryResponseDTO> findByIds(Collection<UUID> portalIds);

}
