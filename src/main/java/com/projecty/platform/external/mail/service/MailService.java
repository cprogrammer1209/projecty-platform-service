package com.projecty.platform.external.mail.service;

import com.projecty.platform.external.mail.dto.EmailMessageDTO;

/**
 * Sends email. Modules depend on this interface, never on the mail provider. Failures are thrown as runtime
 * exceptions.
 */
public interface MailService {

	void send(EmailMessageDTO message);

}
