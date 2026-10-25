package com.projecty.platform.external.mail.service.impl;

import java.nio.charset.StandardCharsets;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import com.projecty.platform.external.mail.config.MailProperties;
import com.projecty.platform.external.mail.dto.EmailMessageDTO;
import com.projecty.platform.external.mail.service.MailService;

import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Sends email over SMTP (Mailpit locally, configured via {@code spring.mail.*}).
 */
@Service
public class SmtpMailServiceImpl implements MailService {

	private final JavaMailSender mailSender;

	private final MailProperties properties;

	public SmtpMailServiceImpl(JavaMailSender mailSender, MailProperties properties) {
		this.mailSender = mailSender;
		this.properties = properties;
	}

	@Override
	public void send(EmailMessageDTO message) {
		try {
			MimeMessage mimeMessage = this.mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
			helper.setFrom(this.properties.from());
			helper.setTo(message.to());
			helper.setSubject(message.subject());
			helper.setText(message.text(), message.html());
			this.mailSender.send(mimeMessage);
		}
		catch (MessagingException ex) {
			throw new MailPreparationException("Could not build email to " + message.to(), ex);
		}
	}

}
