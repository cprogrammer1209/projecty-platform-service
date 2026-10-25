package com.projecty.platform.external.mail.dto;

/**
 * A provider-neutral email: plain text plus an HTML alternative.
 */
public record EmailMessageDTO(String to, String subject, String text, String html) {
}
