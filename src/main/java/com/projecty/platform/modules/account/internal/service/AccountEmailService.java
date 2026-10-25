package com.projecty.platform.modules.account.internal.service;

import com.projecty.platform.modules.account.internal.event.EmailTokenIssuedEvent;

/**
 * Composes and sends the account emails (verification, password reset).
 */
public interface AccountEmailService {

	void onEmailTokenIssued(EmailTokenIssuedEvent event);

}
