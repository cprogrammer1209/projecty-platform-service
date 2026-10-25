package com.projecty.platform.common.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * App-wide settings under {@code projecty.*}. Module settings live in each module's own properties class.
 * {@code refreshCookie.secure} must be true outside local development (HTTPS only).
 */
@ConfigurationProperties("projecty")
public record AppProperties(String frontendUrl, Cors cors, RefreshCookie refreshCookie) {

	public record Cors(List<String> allowedOrigins) {
	}

	public record RefreshCookie(boolean secure) {
	}

}
