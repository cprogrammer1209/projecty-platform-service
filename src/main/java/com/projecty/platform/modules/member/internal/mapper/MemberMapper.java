package com.projecty.platform.modules.member.internal.mapper;

import com.projecty.platform.modules.account.api.dto.UserResponseDTO;
import com.projecty.platform.modules.member.api.dto.PortalMemberResponseDTO;
import com.projecty.platform.modules.member.api.dto.PortalMembershipResponseDTO;
import com.projecty.platform.modules.member.internal.dto.InvitationResponseDTO;
import com.projecty.platform.modules.member.internal.dto.MemberResponseDTO;
import com.projecty.platform.modules.member.internal.dto.MyMembershipResponseDTO;
import com.projecty.platform.modules.member.internal.dto.ProfileResponseDTO;
import com.projecty.platform.modules.member.internal.entity.Invitation;
import com.projecty.platform.modules.member.internal.entity.PortalMember;
import com.projecty.platform.modules.member.internal.entity.Profile;
import com.projecty.platform.modules.portal.api.dto.PortalSummaryResponseDTO;

import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

	public PortalMembershipResponseDTO toMembership(PortalSummaryResponseDTO portal, Profile profile) {
		return new PortalMembershipResponseDTO(portal.id(), portal.name(), portal.slug(), profile.getProfileKey(),
				profile.getName());
	}

	public PortalMemberResponseDTO toPortalMember(PortalMember member, Profile profile) {
		return new PortalMemberResponseDTO(member.getUserId(), profile.getProfileKey(), profile.getName(),
				member.getStatus().name(), member.getJoinedAt());
	}

	public MyMembershipResponseDTO toMine(Profile profile, boolean owner) {
		return new MyMembershipResponseDTO(profile.getId(), profile.getProfileKey(), profile.getName(),
				profile.getPermissions(), owner);
	}

	public MemberResponseDTO toMember(PortalMember member, UserResponseDTO user, Profile profile, boolean owner) {
		return new MemberResponseDTO(member.getUserId(), user.fullName(), user.email(), profile.getId(),
				profile.getProfileKey(), profile.getName(), member.getStatus().name(), owner, user.lastLoginAt(),
				member.getJoinedAt());
	}

	public ProfileResponseDTO toProfile(Profile profile) {
		return new ProfileResponseDTO(profile.getId(), profile.getProfileKey(), profile.getName(),
				profile.getDescription(), profile.getPermissions());
	}

	public InvitationResponseDTO toInvitation(Invitation invitation, Profile profile, String invitedByName) {
		return new InvitationResponseDTO(invitation.getId(), invitation.getEmail(), invitation.getProfileId(),
				profile.getName(), invitedByName, invitation.getCreatedAt(), invitation.getExpiresAt());
	}

}
