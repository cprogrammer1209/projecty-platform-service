package com.projecty.platform.modules.session.internal.service;

import com.projecty.platform.modules.session.api.SessionWriteService;
import com.projecty.platform.modules.session.internal.dto.SessionTokensDTO;

/**
 * Sessions and refresh-token rotation (SEC-US-02, SEC-US-04). Every refresh consumes the presented token and
 * issues a new one. If a consumed token shows up again, someone has a copy of it, so the whole session is
 * revoked.
 *
 * Support sessions (impersonation) are ordinary sessions of the target user that also say which admin is
 * acting; they end when the impersonation ends.
 */
public interface SessionService extends SessionWriteService {

	SessionTokensDTO login(String email, String password, String userAgent, String ip);

	SessionTokensDTO refresh(String refreshToken);

	void logout(String refreshToken);

	/**
	 * Opens a support session for the target user of an impersonation launch code.
	 */
	SessionTokensDTO redeemImpersonation(String code, String userAgent, String ip);

	/**
	 * Ends the support session tied to this refresh token.
	 */
	void endImpersonation(String refreshToken);

}
