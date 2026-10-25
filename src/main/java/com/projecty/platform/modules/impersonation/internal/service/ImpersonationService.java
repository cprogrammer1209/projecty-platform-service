package com.projecty.platform.modules.impersonation.internal.service;

import java.util.UUID;

import com.projecty.platform.modules.impersonation.internal.dto.ImpersonationCreateDTO;
import com.projecty.platform.modules.impersonation.internal.dto.ImpersonationStartResponseDTO;
import com.projecty.platform.modules.session.api.dto.IssuedSessionDTO;

/**
 * Support impersonation: a platform admin signs in to the customer app as a tenant user, for a limited time,
 * with a recorded reason. Every step is audited. The session itself is opened and ended through the session
 * module's API.
 */
public interface ImpersonationService {

	/**
	 * Records the request and returns the one-time launch link for the customer app.
	 */
	ImpersonationStartResponseDTO start(UUID adminId, ImpersonationCreateDTO request);

	/**
	 * Uses up a launch code and opens a support session as the target user.
	 */
	IssuedSessionDTO startSupportSession(String code, String userAgent, String ip);

	/**
	 * Ends the support session behind this refresh token.
	 */
	void endSupportSession(String refreshToken);

	/**
	 * Marks the impersonation ended (idempotent) and records why.
	 */
	void markEnded(UUID impersonationId, String reason);

}
