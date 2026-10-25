package com.projecty.platform.modules.account.internal.service.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.projecty.platform.common.exception.AppException;
import com.projecty.platform.modules.account.internal.config.AccountProperties;
import com.projecty.platform.modules.account.internal.constants.AccountConstants;
import com.projecty.platform.modules.account.internal.entity.AppUser;
import com.projecty.platform.modules.account.internal.entity.EmailToken;
import com.projecty.platform.modules.account.internal.entity.EmailTokenPurpose;
import com.projecty.platform.modules.account.internal.event.EmailTokenIssuedEvent;
import com.projecty.platform.modules.account.internal.repository.EmailTokenRepository;
import com.projecty.platform.modules.account.internal.service.EmailTokenService;
import com.projecty.platform.modules.shared.helper.OpaqueTokenHelper;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailTokenServiceImpl implements EmailTokenService {

	private final EmailTokenRepository repository;

	private final ApplicationEventPublisher events;

	private final AccountProperties properties;

	private final Clock clock;

	public EmailTokenServiceImpl(EmailTokenRepository repository, ApplicationEventPublisher events,
			AccountProperties properties, Clock clock) {
		this.repository = repository;
		this.events = events;
		this.properties = properties;
		this.clock = clock;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void issue(AppUser user, EmailTokenPurpose purpose) {
		Instant now = this.clock.instant();
		this.repository.invalidateUnused(user.getId(), purpose, now);
		String token = OpaqueTokenHelper.generate();
		this.repository
			.save(new EmailToken(user.getId(), purpose, OpaqueTokenHelper.hash(token), now, now.plus(ttl(purpose))));
		this.events.publishEvent(new EmailTokenIssuedEvent(user.getEmail(), user.getFullName(), purpose, token));
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public UUID redeem(String token, EmailTokenPurpose purpose) {
		Instant now = this.clock.instant();
		EmailToken emailToken = this.repository.findByTokenHashAndPurpose(OpaqueTokenHelper.hash(token), purpose)
			.filter((candidate) -> candidate.isUsable(now))
			.orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, AccountConstants.ERROR_TOKEN_INVALID,
					AccountConstants.MESSAGE_TOKEN_INVALID));
		emailToken.markUsed(now);
		return emailToken.getUserId();
	}

	private Duration ttl(EmailTokenPurpose purpose) {
		return switch (purpose) {
			case VERIFY_EMAIL -> this.properties.verifyEmailTokenTtl();
			case RESET_PASSWORD -> this.properties.resetPasswordTokenTtl();
		};
	}

}
