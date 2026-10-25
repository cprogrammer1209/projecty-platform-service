package com.projecty.platform.modules.session.api;

import java.util.UUID;

public interface SessionWriteService {

	/**
	 * Ends every session of the user. Their refresh tokens stop working at once; access tokens already issued
	 * stay valid until they expire.
	 */
	void revokeAllForUser(UUID userId);

}
