package com.projecty.platform.common.config;

import com.projecty.platform.common.security.handler.PermissionInterceptor;
import com.projecty.platform.common.security.handler.RateLimitInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	private final PermissionInterceptor permissionInterceptor;

	private final RateLimitInterceptor rateLimitInterceptor;

	public WebMvcConfig(PermissionInterceptor permissionInterceptor, RateLimitInterceptor rateLimitInterceptor) {
		this.permissionInterceptor = permissionInterceptor;
		this.rateLimitInterceptor = rateLimitInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// Throttle first, so a flood is turned away before any other check or database work.
		registry.addInterceptor(this.rateLimitInterceptor).addPathPatterns("/api/**");
		registry.addInterceptor(this.permissionInterceptor).addPathPatterns("/api/**");
	}

}
