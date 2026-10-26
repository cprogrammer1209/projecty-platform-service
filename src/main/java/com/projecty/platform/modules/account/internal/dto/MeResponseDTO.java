package com.projecty.platform.modules.account.internal.dto;

import java.util.List;
import java.util.UUID;

import com.projecty.platform.modules.member.api.dto.PortalMembershipResponseDTO;

/**
 * The signed-in user, their personal time zone (null = use the portal's) and their portals. An empty {@code portals} list means "show the create-portal screen".
 */
public record MeResponseDTO(UUID id, String email, String fullName, String timeZone,
		List<PortalMembershipResponseDTO> portals) {
}
