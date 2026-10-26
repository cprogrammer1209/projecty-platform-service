package com.projecty.platform.modules.outbox.internal.service.impl;

import java.time.Clock;
import java.util.List;

import com.projecty.platform.external.broker.service.BrokerPublisher;
import com.projecty.platform.modules.outbox.api.dto.OutboxEventCreateDTO;
import com.projecty.platform.modules.outbox.internal.config.OutboxProperties;
import com.projecty.platform.modules.outbox.internal.constants.OutboxConstants;
import com.projecty.platform.modules.outbox.internal.entity.OutboxEvent;
import com.projecty.platform.modules.outbox.internal.repository.OutboxEventRepository;
import com.projecty.platform.modules.outbox.internal.service.OutboxService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class OutboxServiceImpl implements OutboxService {

	private static final Logger log = LoggerFactory.getLogger(OutboxServiceImpl.class);

	private final OutboxEventRepository repository;

	private final BrokerPublisher brokerPublisher;

	private final OutboxProperties properties;

	private final ObjectMapper objectMapper;

	private final Clock clock;

	public OutboxServiceImpl(OutboxEventRepository repository, BrokerPublisher brokerPublisher,
			OutboxProperties properties, ObjectMapper objectMapper, Clock clock) {
		this.repository = repository;
		this.brokerPublisher = brokerPublisher;
		this.properties = properties;
		this.objectMapper = objectMapper;
		this.clock = clock;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void publish(OutboxEventCreateDTO event) {
		String payload;
		try {
			payload = this.objectMapper.writeValueAsString(event.payload());
		}
		catch (JacksonException ex) {
			throw new IllegalArgumentException(OutboxConstants.PAYLOAD_SERIALIZATION_FAILED, ex);
		}
		this.repository.save(new OutboxEvent(event.portalId(), event.aggregateType(), event.aggregateId(),
				event.eventType(), payload, this.clock.instant()));
	}

	@Override
	@Transactional
	public int relayBatch() {
		List<OutboxEvent> events = this.repository.lockOldestUnpublished(this.properties.batchSize());
		int published = 0;
		for (OutboxEvent event : events) {
			try {
				this.brokerPublisher.publish(event.getEventType(), event.getAggregateId().toString(),
						event.getPayload());
			}
			catch (RuntimeException ex) {
				log.warn("Outbox event {} not published, will retry: {}", event.getId(), ex.getMessage());
				break;
			}
			event.markPublished(this.clock.instant());
			published++;
		}
		return published;
	}

}
