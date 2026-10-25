package com.projecty.platform.modules.token.internal.controller;

import java.util.Map;

import com.projecty.platform.modules.token.internal.constants.TokenConstants;
import com.projecty.platform.modules.token.internal.service.SigningKeyService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Publishes the public signing keys so any service can verify access tokens locally. Not wrapped in
 * {@code AppResponse}: JWKS is a standard format that JWT libraries read directly.
 */
@RestController
public class JwksController {

	private final SigningKeyService signingKeyService;

	public JwksController(SigningKeyService signingKeyService) {
		this.signingKeyService = signingKeyService;
	}

	@GetMapping(TokenConstants.JWKS_PATH)
	public Map<String, Object> jwks() {
		return this.signingKeyService.publicKeys().toJSONObject();
	}

}
