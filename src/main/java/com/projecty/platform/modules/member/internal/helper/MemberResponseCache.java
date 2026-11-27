package com.projecty.platform.modules.member.internal.helper;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.projecty.platform.external.redis.config.RedisProperties;
import com.projecty.platform.external.redis.service.CacheService;
import com.projecty.platform.modules.member.internal.constants.MemberConstants;
import com.projecty.platform.modules.member.internal.dto.InvitationResponseDTO;
import com.projecty.platform.modules.member.internal.dto.MemberResponseDTO;
import com.projecty.platform.modules.member.internal.dto.MyMembershipResponseDTO;
import com.projecty.platform.modules.member.internal.dto.ProfileResponseDTO;
import com.projecty.platform.modules.shared.constants.CacheKeyConstants;

import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;

/**
 * Hit-or-miss caching of the member reads: the portals a user belongs to, the user's own membership, and the
 * portal's member, profile and invitation lists. Per-user entries are evicted when that user's membership changes;
 * the two lists carry a portal version in their key, which a member change (or an invitation change) moves. All of it
 * happens after the changing transaction commits, so a rolled-back change evicts nothing. The member list shows each
 * person's last sign-in, which can lag by the TTL.
 */
@Component
public class MemberResponseCache {

	private final CacheService cacheService;

	private final RedisProperties properties;

	public MemberResponseCache(CacheService cacheService, RedisProperties properties) {
		this.cacheService = cacheService;
		this.properties = properties;
	}

	public List<UUID> activePortalIds(UUID userId, Supplier<List<UUID>> loader) {
		return this.cacheService.read(CacheKeyConstants.PORTALS_OF_USER_KEY + userId,
				new TypeReference<List<UUID>>() {
				}, this.properties.responseTtl(), loader);
	}

	public MyMembershipResponseDTO mine(UUID portalId, UUID userId, Supplier<MyMembershipResponseDTO> loader) {
		return this.cacheService.read(CacheKeyConstants.PORTAL_ME_KEY + portalId + CacheKeyConstants.SEPARATOR + userId,
				new TypeReference<MyMembershipResponseDTO>() {
				}, this.properties.responseTtl(), loader);
	}

	public List<MemberResponseDTO> members(UUID portalId, Supplier<List<MemberResponseDTO>> loader) {
		String key = CacheKeyConstants.MEMBERS_KEY + portalId + CacheKeyConstants.SEPARATOR
				+ this.cacheService.version(CacheKeyConstants.MEMBERS_SCOPE + portalId);
		return this.cacheService.read(key, new TypeReference<List<MemberResponseDTO>>() {
		}, this.properties.responseTtl(), loader);
	}

	/** The profiles of a portal are the four system profiles and never change, so they live longer. */
	public List<ProfileResponseDTO> profiles(UUID portalId, Supplier<List<ProfileResponseDTO>> loader) {
		return this.cacheService.read(CacheKeyConstants.PROFILES_KEY + portalId,
				new TypeReference<List<ProfileResponseDTO>>() {
				}, MemberConstants.PROFILES_CACHE_TTL, loader);
	}

	public List<InvitationResponseDTO> invitations(UUID portalId, Supplier<List<InvitationResponseDTO>> loader) {
		String key = CacheKeyConstants.INVITATIONS_KEY + portalId + CacheKeyConstants.SEPARATOR
				+ this.cacheService.version(CacheKeyConstants.INVITATIONS_SCOPE + portalId);
		return this.cacheService.read(key, new TypeReference<List<InvitationResponseDTO>>() {
		}, this.properties.responseTtl(), loader);
	}

	/** A member joined, left, was deactivated or changed profile: drop what depends on that user and on the list. */
	public void memberChanged(UUID portalId, UUID userId) {
		this.cacheService.evictAfterCommit(CacheKeyConstants.PORTALS_OF_USER_KEY + userId,
				CacheKeyConstants.PORTAL_ME_KEY + portalId + CacheKeyConstants.SEPARATOR + userId,
				CacheKeyConstants.ME_KEY + userId);
		this.cacheService.bumpAfterCommit(CacheKeyConstants.MEMBERS_SCOPE + portalId);
	}

	public void invitationsChanged(UUID portalId) {
		this.cacheService.bumpAfterCommit(CacheKeyConstants.INVITATIONS_SCOPE + portalId);
	}

}
