package com.projecty.platform.modules.portal.internal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("projecty.portal")
public record PortalProperties(int maxPortalsPerUser) {
}
