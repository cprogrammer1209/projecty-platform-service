package com.projecty.platform.modules.impersonation.api;

import java.util.UUID;

import com.projecty.platform.modules.impersonation.api.dto.ImpersonationResponseDTO;

public interface ImpersonationWriteService {

	/**
	 * Uses up a one-time launch code. Throws {@code AppException} if the code is unknown, already used or
	 * expired, or the support session can no longer start.
	 */
	ImpersonationResponseDTO redeem(String code);

	/**
	 * Ends the support session (idempotent) and records why.
	 */
	void markEnded(UUID impersonationId, String reason);

}
