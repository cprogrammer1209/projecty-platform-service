package com.projecty.platform.modules.portal.api.dto;

import java.util.UUID;

public record PortalMembershipResponseDTO(UUID id, String name, String slug, String profile) {
}
