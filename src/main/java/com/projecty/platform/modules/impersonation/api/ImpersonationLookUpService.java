package com.projecty.platform.modules.impersonation.api;

import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.modules.impersonation.api.dto.ImpersonationResponseDTO;

public interface ImpersonationLookUpService {

	Optional<ImpersonationResponseDTO> findById(UUID impersonationId);

}
