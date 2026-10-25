package com.projecty.platform.modules.portal.internal.mapper;

import com.projecty.platform.modules.portal.internal.dto.PortalResponseDTO;
import com.projecty.platform.modules.portal.internal.entity.Portal;
import com.projecty.platform.modules.portal.internal.entity.PortalProfile;

import org.springframework.stereotype.Component;

@Component
public class PortalMapper {

	public PortalResponseDTO toResponse(Portal portal, PortalProfile profile) {
		return new PortalResponseDTO(portal.getId(), portal.getName(), portal.getSlug(), profile.name());
	}

}
