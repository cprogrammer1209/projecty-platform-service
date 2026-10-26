package com.projecty.platform.modules.admin.internal.service.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.projecty.platform.common.exception.AppException;
import com.projecty.platform.modules.account.api.UserLookUpService;
import com.projecty.platform.modules.account.api.dto.UserResponseDTO;
import com.projecty.platform.modules.admin.internal.constants.AdminConstants;
import com.projecty.platform.modules.admin.internal.dto.TenantResponseDTO;
import com.projecty.platform.modules.admin.internal.dto.TenantUserResponseDTO;
import com.projecty.platform.modules.admin.internal.mapper.AdminMapper;
import com.projecty.platform.modules.admin.internal.service.AdminTenantService;
import com.projecty.platform.modules.portal.api.PortalLookUpService;
import com.projecty.platform.modules.member.api.MemberLookUpService;
import com.projecty.platform.modules.member.api.dto.MemberCountResponseDTO;
import com.projecty.platform.modules.member.api.dto.PortalMemberResponseDTO;
import com.projecty.platform.modules.portal.api.dto.PortalSummaryResponseDTO;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aggregate service: no data of its own. Joins portals (portal module), their members (member module) and
 * identities (account module) for the admin console.
 */
@Service
public class AdminTenantServiceImpl implements AdminTenantService {

	private final PortalLookUpService portalLookUpService;

	private final MemberLookUpService memberLookUpService;

	private final UserLookUpService userLookUpService;

	private final AdminMapper mapper;

	public AdminTenantServiceImpl(PortalLookUpService portalLookUpService, MemberLookUpService memberLookUpService,
			UserLookUpService userLookUpService, AdminMapper mapper) {
		this.portalLookUpService = portalLookUpService;
		this.memberLookUpService = memberLookUpService;
		this.userLookUpService = userLookUpService;
		this.mapper = mapper;
	}

	@Override
	@Transactional(readOnly = true)
	public List<TenantResponseDTO> listTenants() {
		List<PortalSummaryResponseDTO> portals = this.portalLookUpService.findAllPortals();
		Map<UUID, UserResponseDTO> owners = usersById(
				portals.stream().map(PortalSummaryResponseDTO::ownerUserId).distinct().toList());
		Map<UUID, MemberCountResponseDTO> counts = this.memberLookUpService
			.countMembers(portals.stream().map(PortalSummaryResponseDTO::id).toList());
		return portals.stream()
			.map((portal) -> this.mapper.toTenant(portal, owners.get(portal.ownerUserId()), counts.get(portal.id()),
					null))
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public TenantResponseDTO getTenant(UUID tenantId) {
		PortalSummaryResponseDTO portal = this.portalLookUpService.findPortal(tenantId)
			.orElseThrow(AdminTenantServiceImpl::tenantNotFound);
		List<UUID> memberIds = this.memberLookUpService.findMembers(tenantId)
			.stream()
			.map(PortalMemberResponseDTO::userId)
			.toList();
		Map<UUID, UserResponseDTO> users = usersById(memberIds);
		Instant lastActivity = users.values()
			.stream()
			.map(UserResponseDTO::lastLoginAt)
			.filter(Objects::nonNull)
			.max(Comparator.naturalOrder())
			.orElse(null);
		UserResponseDTO owner = users.containsKey(portal.ownerUserId()) ? users.get(portal.ownerUserId())
				: this.userLookUpService.findById(portal.ownerUserId()).orElse(null);
		return this.mapper.toTenant(portal, owner, this.memberLookUpService.countMembers(List.of(tenantId)).get(tenantId),
				lastActivity);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TenantUserResponseDTO> listUsers(UUID tenantId) {
		if (this.portalLookUpService.findPortal(tenantId).isEmpty()) {
			throw tenantNotFound();
		}
		List<PortalMemberResponseDTO> members = this.memberLookUpService.findMembers(tenantId);
		Map<UUID, UserResponseDTO> users = usersById(members.stream().map(PortalMemberResponseDTO::userId).toList());
		return members.stream()
			.filter((member) -> users.containsKey(member.userId()))
			.map((member) -> this.mapper.toTenantUser(member, users.get(member.userId())))
			.toList();
	}

	private Map<UUID, UserResponseDTO> usersById(List<UUID> userIds) {
		return this.userLookUpService.findByIds(userIds)
			.stream()
			.collect(Collectors.toMap(UserResponseDTO::id, Function.identity()));
	}

	private static AppException tenantNotFound() {
		return new AppException(HttpStatus.NOT_FOUND, AdminConstants.ERROR_TENANT_NOT_FOUND,
				AdminConstants.MESSAGE_TENANT_NOT_FOUND);
	}

}
