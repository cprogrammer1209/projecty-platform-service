package com.projecty.platform.modules.member.internal.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code maxPortalsPerUser}: PTL-BR-10 anti-abuse limit. {@code invitationTtl}: USR-BR-05 (7 days).
 */
@ConfigurationProperties("projecty.member")
public record MemberProperties(int maxPortalsPerUser, Duration invitationTtl) {
}
