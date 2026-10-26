package com.projecty.platform.modules.member.internal.dto;

import java.util.Set;
import java.util.UUID;

public record ProfileResponseDTO(UUID id, String profile, String name, String description, Set<String> permissions) {
}
