package com.projecty.platform.modules.session.internal.mapper;

import com.projecty.platform.modules.session.internal.constants.SessionConstants;
import com.projecty.platform.modules.session.internal.dto.AccessTokenResponseDTO;
import com.projecty.platform.modules.session.internal.dto.SessionTokensDTO;

import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

	public AccessTokenResponseDTO toResponse(SessionTokensDTO tokens) {
		return new AccessTokenResponseDTO(tokens.accessToken().token(), SessionConstants.TOKEN_TYPE,
				tokens.accessToken().expiresInSeconds());
	}

}
