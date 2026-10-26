package com.projecty.platform.modules.outbox.internal.relay;

import com.projecty.platform.modules.outbox.internal.config.OutboxProperties;
import com.projecty.platform.modules.outbox.internal.service.OutboxService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Polls the outbox and hands events to the broker. Delivery is at least once: a crash between publishing and
 * committing {@code published_at} sends the event again, so consumers must tolerate duplicates (SEC-BR-07).
 */
@Component
public class OutboxRelay {

	private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

	private final OutboxService outboxService;

	private final OutboxProperties properties;

	public OutboxRelay(OutboxService outboxService, OutboxProperties properties) {
		this.outboxService = outboxService;
		this.properties = properties;
	}

	@Scheduled(fixedDelayString = "${projecty.outbox.poll-interval}")
	public void relay() {
		if (!this.properties.relayEnabled()) {
			return;
		}
		try {
			while (this.outboxService.relayBatch() >= this.properties.batchSize()) {
				// A full batch means there may be more waiting.
			}
		}
		catch (RuntimeException ex) {
			log.error("Outbox relay run failed", ex);
		}
	}

}
