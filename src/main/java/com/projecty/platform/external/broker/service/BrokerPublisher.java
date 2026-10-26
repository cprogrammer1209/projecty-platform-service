package com.projecty.platform.external.broker.service;

/**
 * Hands a business event to the message broker. The broker is not chosen yet (see the implementation plan),
 * so the only implementation logs the event.
 */
public interface BrokerPublisher {

	/**
	 * @param eventType the event name, for example {@code PortalCreated}
	 * @param key the aggregate id; events with one key must stay in order
	 * @param payload the event as JSON
	 * @throws RuntimeException when the broker did not accept the event; it is then retried
	 */
	void publish(String eventType, String key, String payload);

}
