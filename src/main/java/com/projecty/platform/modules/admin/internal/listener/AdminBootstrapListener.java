package com.projecty.platform.modules.admin.internal.listener;

import com.projecty.platform.modules.admin.internal.service.AdminUserService;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * There is no admin sign-up: the first admin comes from configuration ({@code projecty.admin.bootstrap}).
 */
@Component
public class AdminBootstrapListener {

	private final AdminUserService adminUserService;

	public AdminBootstrapListener(AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void createFirstAdmin() {
		this.adminUserService.bootstrapFirstAdmin();
	}

}
