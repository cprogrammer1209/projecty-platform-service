package com.projecty.platform.external.mail.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("projecty.mail")
public record MailProperties(String from) {
}
