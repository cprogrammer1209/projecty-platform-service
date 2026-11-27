package com.projecty.platform.modules.member.internal.helper;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.projecty.platform.external.redis.config.RedisProperties;
import com.projecty.platform.external.redis.service.KeyValueStore;
import com.projecty.platform.modules.member.internal.constants.MemberConstants;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Remembers whether a user is an active member of a portal and what they may do, so the per-request checks do not
 * hit the database. A change evicts the entry after its transaction commits (a rolled-back change must not
 * evict, and a commit that has not happened must not leave a fresh-looking old value); the TTL covers the rare
 * read that races an eviction and anything changed outside the service, such as a Flyway migration.
 */
@Component
public class MemberAccessCache {

	public record Access(boolean active, Set<String> permissions) {

		public static Access none() {
			return new Access(false, Set.of());
		}

	}

	private final KeyValueStore store;

	private final RedisProperties properties;

	public MemberAccessCache(KeyValueStore store, RedisProperties properties) {
		this.store = store;
		this.properties = properties;
	}

	public Optional<Access> find(UUID portalId, UUID userId) {
		return this.store.get(key(portalId, userId)).map(MemberAccessCache::parse);
	}

	public void put(UUID portalId, UUID userId, Access access) {
		this.store.put(key(portalId, userId), format(access), this.properties.accessTtl());
	}

	/** Evicts once the surrounding transaction has committed (at once when there is none). */
	public void evictAfterCommit(UUID portalId, UUID userId) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			this.store.delete(key(portalId, userId));
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				MemberAccessCache.this.store.delete(key(portalId, userId));
			}
		});
	}

	private static String key(UUID portalId, UUID userId) {
		return MemberConstants.ACCESS_KEY_PREFIX + portalId + MemberConstants.ACCESS_SEPARATOR + userId;
	}

	private static String format(Access access) {
		return (access.active() ? MemberConstants.ACCESS_ACTIVE : MemberConstants.ACCESS_INACTIVE)
				+ MemberConstants.ACCESS_SEPARATOR
				+ access.permissions().stream().sorted().collect(Collectors.joining(MemberConstants.PERMISSION_SEPARATOR));
	}

	private static Access parse(String value) {
		int split = value.indexOf(MemberConstants.ACCESS_SEPARATOR);
		boolean active = value.startsWith(MemberConstants.ACCESS_ACTIVE);
		String rest = split < 0 ? "" : value.substring(split + 1);
		Set<String> permissions = rest.isEmpty() ? Set.of()
				: Arrays.stream(rest.split(MemberConstants.PERMISSION_SEPARATOR)).collect(Collectors.toUnmodifiableSet());
		return new Access(active, permissions);
	}

}
