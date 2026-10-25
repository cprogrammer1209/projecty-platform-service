package com.projecty.platform.modules.admin.internal.service;

import java.util.UUID;

import com.projecty.platform.modules.admin.api.AdminLookUpService;
import com.projecty.platform.modules.admin.api.dto.AdminResponseDTO;
import com.projecty.platform.modules.admin.internal.dto.AdminSessionTokensDTO;

/**
 * Admin identities and admin console sessions.
 */
public interface AdminUserService extends AdminLookUpService {

	AdminSessionTokensDTO login(String email, String password, String userAgent, String ip);

	AdminSessionTokensDTO refresh(String refreshToken);

	void logout(String refreshToken);

	AdminResponseDTO getMe(UUID adminId);

	/**
	 * Creates the configured first admin if there is no admin yet.
	 */
	void bootstrapFirstAdmin();

}
