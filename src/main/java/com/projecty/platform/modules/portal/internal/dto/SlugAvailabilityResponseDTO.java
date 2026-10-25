package com.projecty.platform.modules.portal.internal.dto;

import java.util.List;

public record SlugAvailabilityResponseDTO(boolean available, String reason, List<String> suggestions) {
}
