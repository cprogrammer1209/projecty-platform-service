package com.projecty.platform.modules.member.internal.dto;

import java.util.Set;
import java.util.UUID;

/**
 * The caller's membership in the request's portal. The UI hides actions whose permission isn't listed.
 */
public record MyMembershipResponseDTO(UUID profileId, String profile, String profileName, Set<String> permissions,
		boolean owner) {
}
