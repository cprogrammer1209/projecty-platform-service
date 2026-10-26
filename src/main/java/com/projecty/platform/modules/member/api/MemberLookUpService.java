package com.projecty.platform.modules.member.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.projecty.platform.modules.member.api.dto.MemberCountResponseDTO;
import com.projecty.platform.modules.member.api.dto.PortalMemberResponseDTO;
import com.projecty.platform.modules.member.api.dto.PortalMembershipResponseDTO;

/**
 * Who belongs to which portal. Read-only; membership changes go through the member module's own endpoints.
 */
public interface MemberLookUpService {

	/**
	 * Ids of the portals the user is an active member of, oldest membership first (the token's portals).
	 */
	List<UUID> findActivePortalIds(UUID userId);

	/**
	 * The user's active portals with their profile in each, sorted by portal name.
	 */
	List<PortalMembershipResponseDTO> findMemberships(UUID userId);

	/**
	 * Everyone in the portal, in any status, oldest member first.
	 */
	List<PortalMemberResponseDTO> findMembers(UUID portalId);

	boolean isActiveMember(UUID portalId, UUID userId);

	/**
	 * Member counts per portal; portals without members are missing from the map.
	 */
	Map<UUID, MemberCountResponseDTO> countMembers(Collection<UUID> portalIds);

}
