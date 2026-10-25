package com.projecty.platform.modules.audit.internal.service.impl;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import com.projecty.platform.modules.audit.api.dto.AuditEventCreateDTO;
import com.projecty.platform.modules.audit.internal.constants.AuditConstants;
import com.projecty.platform.modules.audit.internal.dto.AuditEventResponseDTO;
import com.projecty.platform.modules.audit.internal.entity.AuditEvent;
import com.projecty.platform.modules.audit.internal.mapper.AuditEventMapper;
import com.projecty.platform.modules.audit.internal.repository.AuditEventRepository;
import com.projecty.platform.modules.audit.internal.service.AuditEventService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditEventServiceImpl implements AuditEventService {

	private final AuditEventRepository repository;

	private final AuditEventMapper mapper;

	private final Clock clock;

	public AuditEventServiceImpl(AuditEventRepository repository, AuditEventMapper mapper, Clock clock) {
		this.repository = repository;
		this.mapper = mapper;
		this.clock = clock;
	}

	@Override
	@Transactional
	public void record(AuditEventCreateDTO event) {
		this.repository.save(new AuditEvent(this.clock.instant(), event.category(), event.action(),
				event.actorType(), event.actorId(), event.actorName(), event.portalId(), event.targetUserId(),
				event.targetName(), truncate(event.detail())));
	}

	@Override
	@Transactional(readOnly = true)
	public List<AuditEventResponseDTO> findRecent(UUID portalId) {
		Pageable page = PageRequest.of(0, AuditConstants.MAX_EVENTS_LISTED);
		List<AuditEvent> events = (portalId != null)
				? this.repository.findByPortalIdOrderByOccurredAtDesc(portalId, page)
				: this.repository.findByOrderByOccurredAtDesc(page);
		return events.stream().map(this.mapper::toResponse).toList();
	}

	private static String truncate(String detail) {
		return (detail != null && detail.length() > AuditConstants.DETAIL_MAX_LENGTH)
				? detail.substring(0, AuditConstants.DETAIL_MAX_LENGTH) : detail;
	}

}
