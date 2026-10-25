package com.projecty.platform.modules.session.api;

import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.modules.session.api.dto.IssuedSessionDTO;
import com.projecty.platform.modules.session.api.dto.SupportSessionCreateDTO;

public interface SessionWriteService {

	/**
	 * Ends every session of the user. Their refresh tokens stop working at once; access tokens already issued
	 * stay valid until they expire.
	 */
	void revokeAllForUser(UUID userId);

	/**
	 * Opens a support session: a session of the user whose access tokens also carry the acting admin
	 * ({@code act}) and the end time ({@code imp}). It can't be refreshed past {@code expiresAt}.
	 */
	IssuedSessionDTO openSupportSession(SupportSessionCreateDTO request);

	/**
	 * Revokes the session behind this refresh token if it is a support session.
	 * @return the impersonation it belonged to, or empty if the token is unknown or not a support session
	 */
	Optional<UUID> endSupportSession(String refreshToken);

}
