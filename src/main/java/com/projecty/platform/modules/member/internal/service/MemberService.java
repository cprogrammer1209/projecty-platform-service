package com.projecty.platform.modules.member.internal.service;

import java.util.List;
import java.util.UUID;

import com.projecty.platform.modules.member.internal.dto.MemberResponseDTO;
import com.projecty.platform.modules.member.internal.dto.MyMembershipResponseDTO;
import com.projecty.platform.modules.member.internal.dto.ProfileResponseDTO;

/**
 * Managing the people in the request's portal (USR-US-03 to 05). Permissions are checked on the endpoints;
 * the business rules (owner, last Admin) here.
 */
public interface MemberService {

	MyMembershipResponseDTO getMine(UUID userId);

	List<MemberResponseDTO> listMembers();

	List<ProfileResponseDTO> listProfiles();

	MemberResponseDTO changeProfile(UUID actorId, String actorName, UUID userId, UUID profileId);

	MemberResponseDTO deactivate(UUID actorId, String actorName, UUID userId);

	MemberResponseDTO reactivate(UUID actorId, String actorName, UUID userId);

	/**
	 * A new portal: seeds its system profiles and adds the owner as Admin (PTL-US-01), within the portal
	 * limit (PTL-BR-10).
	 */
	void setUpNewPortal(UUID portalId, UUID ownerUserId);

	/**
	 * Adds the user to the portal with the profile, or reactivates them with it if they were deactivated.
	 */
	void addMember(UUID portalId, UUID userId, UUID profileId);

}
