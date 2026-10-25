package com.projecty.platform.modules.account.internal.service;

import java.util.UUID;

import com.projecty.platform.modules.account.api.UserLookUpService;
import com.projecty.platform.modules.account.api.UserWriteService;
import com.projecty.platform.modules.account.internal.dto.MeResponseDTO;
import com.projecty.platform.modules.account.internal.dto.SignupCreateDTO;
import com.projecty.platform.modules.account.internal.dto.UserPreferencesUpdateDTO;

/**
 * Identities: sign up and email verification (SEC-US-01), password reset (SEC-US-03), login checks
 * (SEC-US-02). A new identity has no portal; the user creates one after their first login (PTL-US-01).
 */
public interface UserService extends UserLookUpService, UserWriteService {

	void signup(SignupCreateDTO request);

	void verifyEmail(String token);

	/**
	 * Sends a new verification link if the account exists and is unverified. Says nothing either way.
	 */
	void resendVerification(String email);

	/**
	 * Sends a reset link if the account exists and is active. Says nothing either way.
	 */
	void requestPasswordReset(String email);

	/**
	 * Sets the new password and publishes {@code UserPasswordResetEvent}, which ends every session.
	 */
	void resetPassword(String token, String newPassword);

	MeResponseDTO getMe(UUID userId);

	/**
	 * Sets or clears (null) the user's personal time zone (PTL-US-12).
	 */
	MeResponseDTO updatePreferences(UUID userId, UserPreferencesUpdateDTO request);

}
