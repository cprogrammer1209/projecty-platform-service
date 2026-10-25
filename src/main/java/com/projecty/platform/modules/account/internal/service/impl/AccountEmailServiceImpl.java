package com.projecty.platform.modules.account.internal.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.projecty.platform.common.config.AppProperties;
import com.projecty.platform.external.mail.dto.EmailMessageDTO;
import com.projecty.platform.external.mail.service.MailService;
import com.projecty.platform.modules.account.internal.constants.AccountConstants;
import com.projecty.platform.modules.account.internal.event.EmailTokenIssuedEvent;
import com.projecty.platform.modules.account.internal.service.AccountEmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

/**
 * Runs only after the transaction that created the link commits, so a rolled-back signup never sends an
 * email. A failed send is logged; the user can ask for a new link.
 */
@Service
public class AccountEmailServiceImpl implements AccountEmailService {

	private static final Logger logger = LoggerFactory.getLogger(AccountEmailServiceImpl.class);

	private final MailService mailService;

	private final AppProperties appProperties;

	public AccountEmailServiceImpl(MailService mailService, AppProperties appProperties) {
		this.mailService = mailService;
		this.appProperties = appProperties;
	}

	@Override
	@TransactionalEventListener
	public void onEmailTokenIssued(EmailTokenIssuedEvent event) {
		Content content = switch (event.purpose()) {
			case VERIFY_EMAIL -> new Content(AccountConstants.EMAIL_VERIFY_SUBJECT, AccountConstants.VERIFY_EMAIL_PAGE,
					AccountConstants.EMAIL_VERIFY_INTRO, AccountConstants.EMAIL_VERIFY_ACTION,
					AccountConstants.EMAIL_VERIFY_FOOTER);
			case RESET_PASSWORD -> new Content(AccountConstants.EMAIL_RESET_SUBJECT,
					AccountConstants.RESET_PASSWORD_PAGE, AccountConstants.EMAIL_RESET_INTRO,
					AccountConstants.EMAIL_RESET_ACTION, AccountConstants.EMAIL_RESET_FOOTER);
		};
		String link = this.appProperties.frontendUrl() + "/" + content.page() + "?token="
				+ URLEncoder.encode(event.token(), StandardCharsets.UTF_8);
		try {
			this.mailService.send(new EmailMessageDTO(event.email(), content.subject(),
					text(event.fullName(), content, link), html(event.fullName(), content, link)));
		}
		catch (RuntimeException ex) {
			logger.warn("Could not send {} email to {}", event.purpose(), event.email(), ex);
		}
	}

	private static String text(String name, Content content, String link) {
		return "Hi " + name + ",\n\n" + content.intro() + "\n\n" + content.action() + ": " + link + "\n\n"
				+ content.footer() + "\n\n" + AccountConstants.EMAIL_SIGNATURE + "\n";
	}

	private static String html(String name, Content content, String link) {
		String safeLink = HtmlUtils.htmlEscape(link);
		return """
				<p>Hi %s,</p>
				<p>%s</p>
				<p><a href="%s" style="display:inline-block;padding:10px 18px;background:#2563eb;color:#fff;\
				border-radius:6px;text-decoration:none">%s</a></p>
				<p style="color:#666;font-size:13px">%s<br>Or paste this link into your browser: %s</p>
				<p>%s</p>
				""".formatted(HtmlUtils.htmlEscape(name), content.intro(), safeLink, content.action(),
				content.footer(), safeLink, AccountConstants.EMAIL_SIGNATURE);
	}

	private record Content(String subject, String page, String intro, String action, String footer) {
	}

}
