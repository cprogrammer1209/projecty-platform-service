package com.projecty.platform.common.security.handler;

import java.time.Duration;
import java.util.Map;

import com.projecty.platform.common.config.RateLimitProperties;
import com.projecty.platform.common.constants.RateLimitConstants;
import com.projecty.platform.common.exception.AppException;
import com.projecty.platform.common.security.annotation.RateLimit;
import com.projecty.platform.external.redis.service.KeyValueStore;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Enforces {@link RateLimit} before the controller runs: a fixed window per client IP and limit name, counted in
 * Redis. Errors become AppResponse errors through the global exception handler.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

	private final KeyValueStore store;

	private final RateLimitProperties properties;

	public RateLimitInterceptor(KeyValueStore store, RateLimitProperties properties) {
		this.store = store;
		this.properties = properties;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (!this.properties.enabled() || !(handler instanceof HandlerMethod method)) {
			return true;
		}
		RateLimit annotation = method.getMethodAnnotation(RateLimit.class);
		RateLimitProperties.Rule rule = annotation == null ? null : this.properties.limits().get(annotation.value());
		if (rule == null) {
			return true;
		}
		String key = RateLimitConstants.KEY_PREFIX + annotation.value() + RateLimitConstants.KEY_SEPARATOR
				+ request.getRemoteAddr();
		if (this.store.increment(key, rule.window()) <= rule.limit()) {
			return true;
		}
		long retryAfter = Math.max(1, this.store.timeToLive(key).orElse(rule.window()).toSeconds());
		response.setHeader(RateLimitConstants.HEADER_RETRY_AFTER, String.valueOf(retryAfter));
		throw new AppException(HttpStatus.TOO_MANY_REQUESTS, RateLimitConstants.ERROR_RATE_LIMITED,
				RateLimitConstants.MESSAGE_RATE_LIMITED,
				Map.of(RateLimitConstants.DETAIL_RETRY_AFTER_SECONDS, retryAfter));
	}

}
