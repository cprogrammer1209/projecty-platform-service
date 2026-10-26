package com.projecty.platform.modules.member.internal.listener;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.projecty.platform.common.config.AppProperties;
import com.projecty.platform.external.mail.dto.EmailMessageDTO;
import com.projecty.platform.external.mail.service.MailService;
import com.projecty.platform.modules.member.internal.constants.MemberConstants;
import com.projecty.platform.modules.member.internal.event.InvitationIssuedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

/**
 * Emails the invitation link once the invitation is committed. A mail failure is logged; the admin can
 * resend from the user list.
 */
@Component
public class InvitationEmailListener {

	private static final Logger logger = LoggerFactory.getLogger(InvitationEmailListener.class);

	private final MailService mailService;

	private final AppProperties appProperties;

	public InvitationEmailListener(MailService mailService, AppProperties appProperties) {
		this.mailService = mailService;
		this.appProperties = appProperties;
	}

	@TransactionalEventListener
	public void onInvitationIssued(InvitationIssuedEvent event) {
		String link = this.appProperties.frontendUrl() + "/" + MemberConstants.INVITE_PAGE + "?token="
				+ URLEncoder.encode(event.token(), StandardCharsets.UTF_8);
		String subject = MemberConstants.EMAIL_INVITE_SUBJECT.formatted(event.portalName());
		String intro = MemberConstants.EMAIL_INVITE_INTRO.formatted(event.inviterName(), event.portalName(),
				event.profileName());
		String text = "Hi,\n\n" + intro + "\n\n" + MemberConstants.EMAIL_INVITE_ACTION + ": " + link + "\n\n"
				+ MemberConstants.EMAIL_INVITE_FOOTER + "\n\n" + MemberConstants.EMAIL_SIGNATURE + "\n";
		String safeLink = HtmlUtils.htmlEscape(link);
		String html = """
				<p>Hi,</p>
				<p>%s</p>
				<p><a href="%s" style="display:inline-block;padding:10px 18px;background:#2563eb;color:#fff;\
				border-radius:6px;text-decoration:none">%s</a></p>
				<p style="color:#666;font-size:13px">%s<br>Or paste this link into your browser: %s</p>
				<p>%s</p>
				""".formatted(HtmlUtils.htmlEscape(intro), safeLink, MemberConstants.EMAIL_INVITE_ACTION,
				MemberConstants.EMAIL_INVITE_FOOTER, safeLink, MemberConstants.EMAIL_SIGNATURE);
		try {
			this.mailService.send(new EmailMessageDTO(event.email(), subject, text, html));
		}
		catch (RuntimeException ex) {
			logger.warn("Could not send the invitation email to {}", event.email(), ex);
		}
	}

}
