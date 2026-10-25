package com.projecty.platform.modules.account.api;

import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.modules.account.api.dto.UserResponseDTO;

public interface UserLookUpService {

	Optional<UserResponseDTO> findById(UUID userId);

}
