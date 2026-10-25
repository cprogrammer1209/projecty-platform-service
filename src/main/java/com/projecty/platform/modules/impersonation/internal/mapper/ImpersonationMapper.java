package com.projecty.platform.modules.impersonation.internal.mapper;

import com.projecty.platform.modules.impersonation.api.dto.ImpersonationResponseDTO;
import com.projecty.platform.modules.impersonation.internal.entity.Impersonation;

import org.springframework.stereotype.Component;

@Component
public class ImpersonationMapper {

	public ImpersonationResponseDTO toResponse(Impersonation impersonation, String adminName, String targetName) {
		return new ImpersonationResponseDTO(impersonation.getId(), impersonation.getAdminUserId(), adminName,
				impersonation.getPortalId(), impersonation.getTargetUserId(), targetName,
				impersonation.getExpiresAt(), impersonation.getEndedAt());
	}

}
