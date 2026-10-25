package com.projecty.platform.modules.session.internal.listener;

import com.projecty.platform.modules.account.api.event.UserPasswordResetEvent;
import com.projecty.platform.modules.session.internal.service.SessionService;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * A password reset ends every session (SEC-BR-03). Runs inside the reset transaction, so both commit or
 * neither does.
 */
@Component
public class UserPasswordResetListener {

	private final SessionService sessionService;

	public UserPasswordResetListener(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	@EventListener
	public void onPasswordReset(UserPasswordResetEvent event) {
		this.sessionService.revokeAllForUser(event.userId());
	}

}
