package com.projecty.platform.modules.portal.internal.mapper;

import com.projecty.platform.modules.portal.api.dto.PortalSummaryResponseDTO;
import com.projecty.platform.modules.portal.internal.dto.CurrentPortalResponseDTO;
import com.projecty.platform.modules.portal.internal.dto.PortalResponseDTO;
import com.projecty.platform.modules.portal.internal.entity.Portal;

import org.springframework.stereotype.Component;

@Component
public class PortalMapper {

	public PortalResponseDTO toResponse(Portal portal) {
		return new PortalResponseDTO(portal.getId(), portal.getName(), portal.getSlug());
	}

	public CurrentPortalResponseDTO toCurrent(Portal portal) {
		return new CurrentPortalResponseDTO(portal.getId(), portal.getName(), portal.getSlug(),
				portal.getOwnerUserId(), portal.getTimeZone(), portal.getDateFormat(), portal.getCurrency(),
				portal.getWeekStart().name());
	}

	public PortalSummaryResponseDTO toSummary(Portal portal) {
		return new PortalSummaryResponseDTO(portal.getId(), portal.getName(), portal.getSlug(),
				portal.getOwnerUserId(), portal.getCreatedAt());
	}

}
