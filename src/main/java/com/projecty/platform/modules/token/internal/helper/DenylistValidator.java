package com.projecty.platform.modules.token.internal.helper;

import com.projecty.platform.external.redis.service.KeyValueStore;
import com.projecty.platform.modules.token.internal.constants.TokenConstants;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Refuses an access token whose session was denied (logout, password reset, support session ended), so a revoked
 * session stops working at once instead of when the token expires. A missing {@code sid} claim passes: the
 * denylist only knows sessions.
 */
public class DenylistValidator implements OAuth2TokenValidator<Jwt> {

	private final KeyValueStore keyValueStore;

	public DenylistValidator(KeyValueStore keyValueStore) {
		this.keyValueStore = keyValueStore;
	}

	@Override
	public OAuth2TokenValidatorResult validate(Jwt jwt) {
		String sessionId = jwt.getClaimAsString(TokenConstants.CLAIM_SESSION_ID);
		if (sessionId != null && this.keyValueStore.exists(TokenConstants.DENYLIST_KEY_PREFIX + sessionId)) {
			return OAuth2TokenValidatorResult.failure(
					new OAuth2Error(TokenConstants.ERROR_SESSION_ENDED, TokenConstants.MESSAGE_SESSION_ENDED, null));
		}
		return OAuth2TokenValidatorResult.success();
	}

}
