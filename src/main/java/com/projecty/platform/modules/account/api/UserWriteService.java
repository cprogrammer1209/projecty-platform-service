package com.projecty.platform.modules.account.api;

import com.projecty.platform.modules.account.api.dto.UserResponseDTO;

public interface UserWriteService {

	/**
	 * Checks email and password for a login (SEC-US-02), applying lockout (SEC-BR-02). Failed attempts are
	 * recorded even though an {@code AppException} is thrown, so callers running in a transaction must not roll
	 * back on {@code AppException}.
	 * @return the user, if the password is right and the account is active and verified
	 */
	UserResponseDTO authenticate(String email, String password);

	/**
	 * Creates an identity whose email is already verified, for someone who proved control of the address
	 * another way (an invitation link). Applies the password policy (SEC-BR-01).
	 * @throws com.projecty.platform.common.exception.AppException {@code EMAIL_TAKEN} or {@code WEAK_PASSWORD}
	 */
	UserResponseDTO createVerifiedUser(String email, String fullName, String password);

}
