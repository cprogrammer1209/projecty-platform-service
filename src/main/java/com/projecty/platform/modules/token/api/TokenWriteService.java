package com.projecty.platform.modules.token.api;

import com.projecty.platform.modules.token.api.dto.AccessTokenClaimsDTO;
import com.projecty.platform.modules.token.api.dto.IssuedAccessTokenDTO;

public interface TokenWriteService {

	/**
	 * Signs a short-lived access token (JWT) for the given user and session.
	 */
	IssuedAccessTokenDTO issueAccessToken(AccessTokenClaimsDTO claims);

}
