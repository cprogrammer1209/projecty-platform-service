package com.projecty.platform.modules.account.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.modules.account.api.dto.UserResponseDTO;

public interface UserLookUpService {

	Optional<UserResponseDTO> findById(UUID userId);

	/**
	 * The users that exist among {@code userIds}, in no particular order.
	 */
	List<UserResponseDTO> findByIds(Collection<UUID> userIds);

	/**
	 * Case-insensitive.
	 */
	Optional<UserResponseDTO> findByEmail(String email);

}
