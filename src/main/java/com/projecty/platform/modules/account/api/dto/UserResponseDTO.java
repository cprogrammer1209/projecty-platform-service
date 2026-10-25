package com.projecty.platform.modules.account.api.dto;

import java.util.UUID;

public record UserResponseDTO(UUID id, String email, String fullName, boolean active) {
}
