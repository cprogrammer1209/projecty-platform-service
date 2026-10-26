package com.projecty.platform.modules.member.internal.listener;

import com.projecty.platform.modules.member.internal.service.MemberService;
import com.projecty.platform.modules.portal.api.event.PortalCreatedEvent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Runs in the portal-creating transaction, so a failure here (e.g. the portal limit) cancels the portal.
 */
@Component
public class PortalCreatedListener {

	private final MemberService memberService;

	public PortalCreatedListener(MemberService memberService) {
		this.memberService = memberService;
	}

	@EventListener
	public void onPortalCreated(PortalCreatedEvent event) {
		this.memberService.setUpNewPortal(event.portalId(), event.ownerUserId());
	}

}
