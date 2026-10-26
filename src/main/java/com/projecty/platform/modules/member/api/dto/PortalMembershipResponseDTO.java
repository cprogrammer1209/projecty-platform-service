package com.projecty.platform.modules.member.api.dto;

import java.util.UUID;

/**
 * One of a user's active portals, with their profile in it ({@code profile} is the key, e.g. {@code ADMIN}).
 */
public record PortalMembershipResponseDTO(UUID id, String name, String slug, String profile, String profileName) {
}
