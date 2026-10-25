package com.projecty.platform.modules.impersonation.internal.service;

import java.util.UUID;

import com.projecty.platform.modules.impersonation.api.ImpersonationLookUpService;
import com.projecty.platform.modules.impersonation.api.ImpersonationWriteService;
import com.projecty.platform.modules.impersonation.internal.dto.ImpersonationCreateDTO;
import com.projecty.platform.modules.impersonation.internal.dto.ImpersonationStartResponseDTO;

/**
 * Support impersonation: a platform admin signs in to the customer app as a tenant user, for a limited time,
 * with a recorded reason. Every step is audited.
 */
public interface ImpersonationService extends ImpersonationLookUpService, ImpersonationWriteService {

	/**
	 * Records the request and returns the one-time launch link for the customer app.
	 */
	ImpersonationStartResponseDTO start(UUID adminId, ImpersonationCreateDTO request);

}
