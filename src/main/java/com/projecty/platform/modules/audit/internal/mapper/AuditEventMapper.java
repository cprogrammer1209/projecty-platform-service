package com.projecty.platform.modules.audit.internal.mapper;

import com.projecty.platform.modules.audit.internal.dto.AuditEventResponseDTO;
import com.projecty.platform.modules.audit.internal.entity.AuditEvent;

import org.springframework.stereotype.Component;

@Component
public class AuditEventMapper {

	public AuditEventResponseDTO toResponse(AuditEvent event) {
		return new AuditEventResponseDTO(event.getId(), event.getOccurredAt(), event.getCategory(),
				event.getAction(), event.getActorType(), event.getActorId(), event.getActorName(),
				event.getPortalId(), event.getTargetUserId(), event.getTargetName(), event.getDetail());
	}

}
