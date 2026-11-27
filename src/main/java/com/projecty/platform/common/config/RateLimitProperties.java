package com.projecty.platform.common.config;

import java.time.Duration;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param enabled switch for every limit (the tests turn it off)
 * @param limits by name (see {@code RateLimitConstants}); a name with no entry is not limited
 */
@ConfigurationProperties("projecty.rate-limit")
public record RateLimitProperties(boolean enabled, Map<String, Rule> limits) {

	/**
	 * @param limit how many calls one client may make in a window
	 * @param window how long the count lives, from the first call
	 */
	public record Rule(int limit, Duration window) {
	}

	public RateLimitProperties {
		limits = limits == null ? Map.of() : limits;
	}

}
