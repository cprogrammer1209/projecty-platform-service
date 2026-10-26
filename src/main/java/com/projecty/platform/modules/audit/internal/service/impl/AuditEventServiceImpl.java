package com.projecty.platform.modules.audit.internal.service.impl;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import com.projecty.platform.common.dto.PagedResponse;
import com.projecty.platform.common.service.RequestPortalContext;
import com.projecty.platform.modules.audit.api.dto.AuditEventCreateDTO;
import com.projecty.platform.modules.audit.internal.constants.AuditConstants;
import com.projecty.platform.modules.audit.internal.dto.AuditEventResponseDTO;
import com.projecty.platform.modules.audit.internal.entity.AuditEvent;
import com.projecty.platform.modules.audit.internal.mapper.AuditEventMapper;
import com.projecty.platform.modules.audit.internal.repository.AuditEventRepository;
import com.projecty.platform.modules.audit.internal.service.AuditEventService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditEventServiceImpl implements AuditEventService {

	private final AuditEventRepository repository;

	private final AuditEventMapper mapper;

	private final RequestPortalContext portalContext;

	private final Clock clock;

	public AuditEventServiceImpl(AuditEventRepository repository, AuditEventMapper mapper,
			RequestPortalContext portalContext, Clock clock) {
		this.repository = repository;
		this.mapper = mapper;
		this.portalContext = portalContext;
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

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<AuditEventResponseDTO> findForCurrentPortal(String category, String action, int page,
			int size) {
		UUID portalId = this.portalContext.requirePortalId();
		Specification<AuditEvent> spec = (root, query, cb) -> cb.equal(root.get("portalId"), portalId);
		if (category != null && !category.isBlank()) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category.strip()));
		}
		if (action != null && !action.isBlank()) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("action"), action.strip()));
		}
		int pageSize = Math.min(Math.max(size, 1), AuditConstants.MAX_PAGE_SIZE);
		Pageable pageable = PageRequest.of(Math.max(page, 0), pageSize, Sort.by(Sort.Direction.DESC, "occurredAt"));
		Page<AuditEvent> result = this.repository.findAll(spec, pageable);
		return new PagedResponse<>(result.map(this.mapper::toResponse).getContent(), result.getNumber(),
				result.getSize(), result.getTotalElements(), result.getTotalPages());
	}

	private static String truncate(String detail) {
		return (detail != null && detail.length() > AuditConstants.DETAIL_MAX_LENGTH)
				? detail.substring(0, AuditConstants.DETAIL_MAX_LENGTH) : detail;
	}

}
