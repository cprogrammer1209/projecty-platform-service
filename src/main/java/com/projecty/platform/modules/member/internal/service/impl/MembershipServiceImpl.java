package com.projecty.platform.modules.member.internal.service.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.projecty.platform.modules.member.api.dto.MemberCountResponseDTO;
import com.projecty.platform.modules.member.api.dto.PortalMemberResponseDTO;
import com.projecty.platform.modules.member.api.dto.PortalMembershipResponseDTO;
import com.projecty.platform.modules.member.internal.entity.MemberStatus;
import com.projecty.platform.modules.member.internal.entity.PortalMember;
import com.projecty.platform.modules.member.internal.entity.Profile;
import com.projecty.platform.modules.member.internal.mapper.MemberMapper;
import com.projecty.platform.modules.member.internal.repository.PortalMemberRepository;
import com.projecty.platform.modules.member.internal.repository.ProfileRepository;
import com.projecty.platform.modules.member.internal.service.MembershipService;
import com.projecty.platform.modules.portal.api.PortalLookUpService;
import com.projecty.platform.modules.portal.api.dto.PortalSummaryResponseDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MembershipServiceImpl implements MembershipService {

	private final PortalMemberRepository memberRepository;

	private final ProfileRepository profileRepository;

	private final PortalLookUpService portalLookUpService;

	private final MemberMapper mapper;

	public MembershipServiceImpl(PortalMemberRepository memberRepository, ProfileRepository profileRepository,
			PortalLookUpService portalLookUpService, MemberMapper mapper) {
		this.memberRepository = memberRepository;
		this.profileRepository = profileRepository;
		this.portalLookUpService = portalLookUpService;
		this.mapper = mapper;
	}

	@Override
	@Transactional(readOnly = true)
	public List<UUID> findActivePortalIds(UUID userId) {
		return this.memberRepository.findActivePortalIds(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<PortalMembershipResponseDTO> findMemberships(UUID userId) {
		List<PortalMember> members = this.memberRepository.findByUserIdAndStatus(userId, MemberStatus.ACTIVE);
		Map<UUID, Profile> profiles = profilesById(members.stream().map(PortalMember::getProfileId).toList());
		Map<UUID, PortalSummaryResponseDTO> portals = this.portalLookUpService
			.findByIds(members.stream().map(PortalMember::getPortalId).toList())
			.stream()
			.collect(Collectors.toMap(PortalSummaryResponseDTO::id, Function.identity()));
		return members.stream()
			.filter((m) -> portals.containsKey(m.getPortalId()))
			.map((m) -> this.mapper.toMembership(portals.get(m.getPortalId()), profiles.get(m.getProfileId())))
			.sorted(Comparator.comparing(PortalMembershipResponseDTO::name, String.CASE_INSENSITIVE_ORDER))
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<PortalMemberResponseDTO> findMembers(UUID portalId) {
		List<PortalMember> members = this.memberRepository.findByPortalIdOrderByJoinedAt(portalId);
		Map<UUID, Profile> profiles = profilesById(members.stream().map(PortalMember::getProfileId).toList());
		return members.stream().map((m) -> this.mapper.toPortalMember(m, profiles.get(m.getProfileId()))).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isActiveMember(UUID portalId, UUID userId) {
		return this.memberRepository.existsByPortalIdAndUserIdAndStatus(portalId, userId, MemberStatus.ACTIVE);
	}

	@Override
	@Transactional(readOnly = true)
	public Map<UUID, MemberCountResponseDTO> countMembers(Collection<UUID> portalIds) {
		return this.memberRepository.findByPortalIdIn(portalIds)
			.stream()
			.collect(Collectors.groupingBy(PortalMember::getPortalId,
					Collectors.collectingAndThen(Collectors.toList(), (list) -> new MemberCountResponseDTO(list.size(),
							list.stream().filter(PortalMember::isActive).count()))));
	}

	@Override
	@Transactional(readOnly = true)
	public Set<String> permissions(UUID portalId, UUID userId) {
		return this.memberRepository.findByPortalIdAndUserId(portalId, userId)
			.filter(PortalMember::isActive)
			.flatMap((m) -> this.profileRepository.findById(m.getProfileId()))
			.map((profile) -> Set.copyOf(profile.getPermissions()))
			.orElse(Set.of());
	}

	private Map<UUID, Profile> profilesById(List<UUID> profileIds) {
		return this.profileRepository.findAllById(profileIds)
			.stream()
			.collect(Collectors.toMap(Profile::getId, Function.identity()));
	}

}
