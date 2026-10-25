package com.projecty.platform.modules.account.internal.service;

import java.util.UUID;

import com.projecty.platform.modules.account.internal.entity.AppUser;
import com.projecty.platform.modules.account.internal.entity.EmailTokenPurpose;

/**
 * Single-use email links (SEC-BR-04). Issuing a new link invalidates older unused links of the same purpose.
 */
public interface EmailTokenService {

	/**
	 * Creates a link and schedules its email for after the current transaction commits.
	 */
	void issue(AppUser user, EmailTokenPurpose purpose);

	/**
	 * Marks the token used and returns its user id.
	 */
	UUID redeem(String token, EmailTokenPurpose purpose);

}
