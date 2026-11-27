package com.projecty.platform.modules.portal.internal.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import com.projecty.platform.external.redis.config.RedisProperties;
import com.projecty.platform.external.redis.service.CacheService;
import com.projecty.platform.modules.portal.api.dto.PortalSummaryResponseDTO;
import com.projecty.platform.modules.portal.internal.dto.CurrentPortalResponseDTO;
import com.projecty.platform.modules.shared.constants.CacheKeyConstants;

import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;

/**
 * Hit-or-miss caching of what other modules and screens read about a portal on almost every request: its summary
 * (name, slug, owner) and its settings. Portals change rarely, so a change just evicts the two entries once its
 * transaction has committed; the TTL covers anything else.
 */
@Component
public class PortalCache {

	private final CacheService cacheService;

	private final RedisProperties properties;

	public PortalCache(CacheService cacheService, RedisProperties properties) {
		this.cacheService = cacheService;
		this.properties = properties;
	}

	public Optional<PortalSummaryResponseDTO> findPortal(UUID portalId,
			Supplier<Optional<PortalSummaryResponseDTO>> loader) {
		return Optional.ofNullable(this.cacheService.read(CacheKeyConstants.PORTAL_KEY + portalId,
				new TypeReference<PortalSummaryResponseDTO>() {
				}, this.properties.responseTtl(), () -> loader.get().orElse(null)));
	}

	/** One lookup per cached portal and a single database call for all the others. */
	public List<PortalSummaryResponseDTO> findByIds(Collection<UUID> portalIds,
			Function<Collection<UUID>, List<PortalSummaryResponseDTO>> loader) {
		List<PortalSummaryResponseDTO> found = new ArrayList<>();
		List<UUID> missing = new ArrayList<>();
		for (UUID id : portalIds) {
			Optional<PortalSummaryResponseDTO> cached = this.cacheService.peek(CacheKeyConstants.PORTAL_KEY + id,
					new TypeReference<PortalSummaryResponseDTO>() {
					});
			if (cached.isPresent()) {
				found.add(cached.get());
			}
			else {
				missing.add(id);
			}
		}
		if (!missing.isEmpty()) {
			for (PortalSummaryResponseDTO portal : loader.apply(missing)) {
				this.cacheService.put(CacheKeyConstants.PORTAL_KEY + portal.id(), portal, this.properties.responseTtl());
				found.add(portal);
			}
		}
		return found;
	}

	public CurrentPortalResponseDTO current(UUID portalId, Supplier<CurrentPortalResponseDTO> loader) {
		return this.cacheService.read(CacheKeyConstants.PORTAL_CURRENT_KEY + portalId,
				new TypeReference<CurrentPortalResponseDTO>() {
				}, this.properties.responseTtl(), loader);
	}

	public void changedAfterCommit(UUID portalId) {
		this.cacheService.evictAfterCommit(CacheKeyConstants.PORTAL_KEY + portalId,
				CacheKeyConstants.PORTAL_CURRENT_KEY + portalId);
	}

}
