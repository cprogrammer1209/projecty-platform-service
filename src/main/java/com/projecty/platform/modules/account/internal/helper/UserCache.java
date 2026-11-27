package com.projecty.platform.modules.account.internal.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import com.projecty.platform.external.redis.config.RedisProperties;
import com.projecty.platform.external.redis.service.CacheService;
import com.projecty.platform.modules.account.api.dto.UserResponseDTO;
import com.projecty.platform.modules.account.internal.dto.MeResponseDTO;
import com.projecty.platform.modules.shared.constants.CacheKeyConstants;

import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;

/**
 * Hit-or-miss caching of what is read about a user over and over: the user record other modules look up (member
 * lists, session refresh) and the user's own {@code /api/me}. A change to the user evicts both after its transaction
 * commits; a change to the user's memberships evicts {@code /api/me} from the member module by the same key.
 */
@Component
public class UserCache {

	private final CacheService cacheService;

	private final RedisProperties properties;

	public UserCache(CacheService cacheService, RedisProperties properties) {
		this.cacheService = cacheService;
		this.properties = properties;
	}

	public Optional<UserResponseDTO> findById(UUID userId, Supplier<Optional<UserResponseDTO>> loader) {
		return Optional.ofNullable(this.cacheService.read(CacheKeyConstants.USER_KEY + userId,
				new TypeReference<UserResponseDTO>() {
				}, this.properties.responseTtl(), () -> loader.get().orElse(null)));
	}

	/** One lookup per cached user and a single database call for all the others. */
	public List<UserResponseDTO> findByIds(Collection<UUID> userIds,
			Function<Collection<UUID>, List<UserResponseDTO>> loader) {
		List<UserResponseDTO> found = new ArrayList<>();
		List<UUID> missing = new ArrayList<>();
		for (UUID id : userIds) {
			Optional<UserResponseDTO> cached = this.cacheService.peek(CacheKeyConstants.USER_KEY + id,
					new TypeReference<UserResponseDTO>() {
					});
			if (cached.isPresent()) {
				found.add(cached.get());
			}
			else {
				missing.add(id);
			}
		}
		if (!missing.isEmpty()) {
			for (UserResponseDTO user : loader.apply(missing)) {
				this.cacheService.put(CacheKeyConstants.USER_KEY + user.id(), user, this.properties.responseTtl());
				found.add(user);
			}
		}
		return found;
	}

	public MeResponseDTO me(UUID userId, Supplier<MeResponseDTO> loader) {
		return this.cacheService.read(CacheKeyConstants.ME_KEY + userId, new TypeReference<MeResponseDTO>() {
		}, this.properties.responseTtl(), loader);
	}

	public void changedAfterCommit(UUID userId) {
		this.cacheService.evictAfterCommit(CacheKeyConstants.USER_KEY + userId, CacheKeyConstants.ME_KEY + userId);
	}

}
