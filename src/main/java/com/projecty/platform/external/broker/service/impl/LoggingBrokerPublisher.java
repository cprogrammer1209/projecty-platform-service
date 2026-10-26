package com.projecty.platform.external.broker.service.impl;

import com.projecty.platform.external.broker.service.BrokerPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingBrokerPublisher implements BrokerPublisher {

	private static final Logger log = LoggerFactory.getLogger(LoggingBrokerPublisher.class);

	@Override
	public void publish(String eventType, String key, String payload) {
		log.info("Business event {} key={} payload={}", eventType, key, payload);
	}

}
