package com.projecty.platform.modules.session.internal.service;

import com.projecty.platform.modules.session.api.SessionWriteService;
import com.projecty.platform.modules.session.internal.dto.SessionTokensDTO;

/**
 * Sessions and refresh-token rotation (SEC-US-02, SEC-US-04). Every refresh consumes the presented token and
 * issues a new one. If a consumed token shows up again, someone has a copy of it, so the whole session is
 * revoked.
 */
public interface SessionService extends SessionWriteService {

	SessionTokensDTO login(String email, String password, String userAgent, String ip);

	SessionTokensDTO refresh(String refreshToken);

	void logout(String refreshToken);

}
