package com.projecty.platform.modules.account.internal.mapper;

import java.util.List;

import com.projecty.platform.modules.account.api.dto.UserResponseDTO;
import com.projecty.platform.modules.account.internal.dto.MeResponseDTO;
import com.projecty.platform.modules.account.internal.entity.AppUser;
import com.projecty.platform.modules.portal.api.dto.PortalMembershipResponseDTO;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

	public UserResponseDTO toResponse(AppUser user) {
		return new UserResponseDTO(user.getId(), user.getEmail(), user.getFullName(), user.isActive(),
				user.getLastLoginAt());
	}

	public MeResponseDTO toMe(AppUser user, List<PortalMembershipResponseDTO> portals) {
		return new MeResponseDTO(user.getId(), user.getEmail(), user.getFullName(), user.getTimeZone(), portals);
	}

}
