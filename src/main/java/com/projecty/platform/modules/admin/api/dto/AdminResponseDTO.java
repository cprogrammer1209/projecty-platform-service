package com.projecty.platform.modules.admin.api.dto;

import java.util.UUID;

public record AdminResponseDTO(UUID id, String email, String fullName, boolean active) {
}
