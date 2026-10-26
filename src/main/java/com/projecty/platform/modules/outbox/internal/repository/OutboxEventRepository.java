package com.projecty.platform.modules.outbox.internal.repository;

import java.util.List;
import java.util.UUID;

import com.projecty.platform.modules.outbox.internal.entity.OutboxEvent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

	/**
	 * The oldest unpublished events, locked so that a second relay instance skips them instead of
	 * publishing them twice at the same time. Ids are time-ordered (UUIDv7), so id order is creation order.
	 */
	@Query(value = "SELECT * FROM outbox_event WHERE published_at IS NULL ORDER BY id LIMIT :limit "
			+ "FOR UPDATE SKIP LOCKED", nativeQuery = true)
	List<OutboxEvent> lockOldestUnpublished(@Param("limit") int limit);

}
