package com.projecty.platform.modules.audit.api;

import com.projecty.platform.modules.audit.api.dto.AuditEventCreateDTO;

public interface AuditWriteService {

	/**
	 * Appends an event. Runs in the caller's transaction, so it is only kept if the audited action is.
	 */
	void record(AuditEventCreateDTO event);

}
