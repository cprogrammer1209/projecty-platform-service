package com.projecty.platform.modules.session.internal.mapper;

import com.projecty.platform.modules.session.api.dto.IssuedSessionDTO;
import com.projecty.platform.modules.session.internal.constants.SessionConstants;
import com.projecty.platform.modules.session.internal.dto.AccessTokenResponseDTO;
import com.projecty.platform.modules.session.internal.dto.SessionTokensDTO;

import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

	public IssuedSessionDTO toIssued(SessionTokensDTO tokens) {
		return new IssuedSessionDTO(tokens.accessToken().token(), tokens.accessToken().expiresInSeconds(),
				tokens.refreshToken(), tokens.refreshExpiresAt());
	}

	public AccessTokenResponseDTO toResponse(SessionTokensDTO tokens) {
		return new AccessTokenResponseDTO(tokens.accessToken().token(), SessionConstants.TOKEN_TYPE,
				tokens.accessToken().expiresInSeconds());
	}

}
