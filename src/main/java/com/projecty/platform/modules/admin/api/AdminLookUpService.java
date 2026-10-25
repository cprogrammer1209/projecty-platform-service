package com.projecty.platform.modules.admin.api;

import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.modules.admin.api.dto.AdminResponseDTO;

public interface AdminLookUpService {

	Optional<AdminResponseDTO> findById(UUID adminId);

}
