package com.projecty.platform.common.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Limits how often one client (by IP address) may call this endpoint, using the named limit from
 * {@code projecty.rate-limit.limits} (a name from {@code RateLimitConstants}). Over the limit the request gets 429
 * with a {@code Retry-After} header. If Redis is down nothing is limited.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

	String value();

}
