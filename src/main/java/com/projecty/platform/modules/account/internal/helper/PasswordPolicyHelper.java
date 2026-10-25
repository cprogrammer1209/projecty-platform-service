package com.projecty.platform.modules.account.internal.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.projecty.platform.common.exception.AppException;
import com.projecty.platform.modules.account.internal.constants.AccountConstants;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * SEC-BR-01: at least 10 characters and not a commonly used (breached) password. No composition rules and no
 * forced rotation, following current NIST guidance.
 */
@Component
public class PasswordPolicyHelper {

	private final Set<String> commonPasswords;

	public PasswordPolicyHelper() {
		this.commonPasswords = load(AccountConstants.COMMON_PASSWORDS_RESOURCE);
	}

	public void check(String password, String email) {
		if (password == null || password.length() < AccountConstants.PASSWORD_MIN_LENGTH) {
			throw weak(AccountConstants.MESSAGE_PASSWORD_TOO_SHORT);
		}
		if (password.length() > AccountConstants.PASSWORD_MAX_LENGTH) {
			throw weak(AccountConstants.MESSAGE_PASSWORD_TOO_LONG);
		}
		String lower = password.toLowerCase(Locale.ROOT);
		if (this.commonPasswords.contains(lower)) {
			throw weak(AccountConstants.MESSAGE_PASSWORD_COMMON);
		}
		String emailName = (email != null) ? email.toLowerCase(Locale.ROOT).split("@")[0] : "";
		if (emailName.length() >= AccountConstants.EMAIL_NAME_MIN_LENGTH && lower.contains(emailName)) {
			throw weak(AccountConstants.MESSAGE_PASSWORD_CONTAINS_EMAIL);
		}
	}

	private static AppException weak(String message) {
		return new AppException(HttpStatus.BAD_REQUEST, AccountConstants.ERROR_WEAK_PASSWORD, message);
	}

	private static Set<String> load(String path) {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8))) {
			return reader.lines()
				.filter((line) -> !line.startsWith(AccountConstants.COMMON_PASSWORDS_COMMENT_PREFIX))
				.collect(Collectors.toUnmodifiableSet());
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

}
