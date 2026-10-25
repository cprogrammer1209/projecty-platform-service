package com.projecty.platform.modules.admin.internal.mapper;

import java.time.Instant;

import com.projecty.platform.modules.account.api.dto.UserResponseDTO;
import com.projecty.platform.modules.admin.api.dto.AdminResponseDTO;
import com.projecty.platform.modules.admin.internal.constants.AdminConstants;
import com.projecty.platform.modules.admin.internal.dto.AdminAccessTokenResponseDTO;
import com.projecty.platform.modules.admin.internal.dto.AdminSessionTokensDTO;
import com.projecty.platform.modules.admin.internal.dto.TenantResponseDTO;
import com.projecty.platform.modules.admin.internal.dto.TenantUserResponseDTO;
import com.projecty.platform.modules.admin.internal.entity.AdminUser;
import com.projecty.platform.modules.portal.api.dto.PortalMemberResponseDTO;
import com.projecty.platform.modules.portal.api.dto.PortalSummaryResponseDTO;

import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

	public AdminResponseDTO toResponse(AdminUser admin) {
		return new AdminResponseDTO(admin.getId(), admin.getEmail(), admin.getFullName(), admin.isActive());
	}

	public AdminAccessTokenResponseDTO toResponse(AdminSessionTokensDTO tokens) {
		return new AdminAccessTokenResponseDTO(tokens.accessToken().token(), AdminConstants.TOKEN_TYPE,
				tokens.accessToken().expiresInSeconds());
	}

	/**
	 * @param owner null if the owner's identity no longer exists
	 */
	public TenantResponseDTO toTenant(PortalSummaryResponseDTO portal, UserResponseDTO owner,
			Instant lastActivityAt) {
		return new TenantResponseDTO(portal.id(), portal.name(), portal.slug(), portal.ownerUserId(),
				(owner != null) ? owner.fullName() : null, (owner != null) ? owner.email() : null, portal.createdAt(),
				portal.memberCount(), portal.activeMemberCount(), lastActivityAt);
	}

	public TenantUserResponseDTO toTenantUser(PortalMemberResponseDTO member, UserResponseDTO user) {
		boolean active = user.active() && AdminConstants.MEMBER_STATUS_ACTIVE.equals(member.status());
		return new TenantUserResponseDTO(user.id(), user.fullName(), user.email(), member.profile(), active,
				user.lastLoginAt(), member.joinedAt());
	}

}
