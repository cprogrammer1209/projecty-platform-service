package com.projecty.platform.modules.impersonation.internal.listener;

import com.projecty.platform.modules.impersonation.internal.constants.ImpersonationConstants;
import com.projecty.platform.modules.impersonation.internal.service.ImpersonationService;
import com.projecty.platform.modules.session.api.event.SupportSessionExpiredEvent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Records the end of a support session that ran out of time. Runs in the session module's refresh
 * transaction.
 */
@Component
public class SupportSessionExpiredListener {

	private final ImpersonationService impersonationService;

	public SupportSessionExpiredListener(ImpersonationService impersonationService) {
		this.impersonationService = impersonationService;
	}

	@EventListener
	public void onExpired(SupportSessionExpiredEvent event) {
		this.impersonationService.markEnded(event.impersonationId(), ImpersonationConstants.END_REASON_EXPIRED);
	}

}
