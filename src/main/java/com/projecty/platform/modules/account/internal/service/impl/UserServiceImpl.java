package com.projecty.platform.modules.account.internal.service.impl;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.projecty.platform.common.constants.AppConstants;
import com.projecty.platform.common.exception.AppException;
import com.projecty.platform.modules.account.api.dto.UserResponseDTO;
import com.projecty.platform.modules.account.api.event.UserPasswordResetEvent;
import com.projecty.platform.modules.account.internal.config.AccountProperties;
import com.projecty.platform.modules.account.internal.constants.AccountConstants;
import com.projecty.platform.modules.account.internal.dto.MeResponseDTO;
import com.projecty.platform.modules.account.internal.dto.SignupCreateDTO;
import com.projecty.platform.modules.account.internal.dto.UserPreferencesUpdateDTO;
import com.projecty.platform.modules.account.internal.entity.AppUser;
import com.projecty.platform.modules.account.internal.entity.EmailTokenPurpose;
import com.projecty.platform.modules.account.internal.helper.PasswordPolicyHelper;
import com.projecty.platform.modules.account.internal.mapper.UserMapper;
import com.projecty.platform.modules.account.internal.repository.AppUserRepository;
import com.projecty.platform.modules.account.internal.service.EmailTokenService;
import com.projecty.platform.modules.account.internal.service.UserService;
import com.projecty.platform.modules.portal.api.PortalLookUpService;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Composite service: owns {@code AppUser} and reads portal memberships through the portal module's API.
 */
@Service
public class UserServiceImpl implements UserService {

	private final AppUserRepository repository;

	private final EmailTokenService emailTokenService;

	private final PortalLookUpService portalLookUpService;

	private final PasswordEncoder passwordEncoder;

	private final PasswordPolicyHelper passwordPolicy;

	private final UserMapper userMapper;

	private final ApplicationEventPublisher events;

	private final AccountProperties properties;

	private final Clock clock;

	private final String timingDummyHash;

	public UserServiceImpl(AppUserRepository repository, EmailTokenService emailTokenService,
			PortalLookUpService portalLookUpService, PasswordEncoder passwordEncoder,
			PasswordPolicyHelper passwordPolicy, UserMapper userMapper, ApplicationEventPublisher events,
			AccountProperties properties, Clock clock) {
		this.repository = repository;
		this.emailTokenService = emailTokenService;
		this.portalLookUpService = portalLookUpService;
		this.passwordEncoder = passwordEncoder;
		this.passwordPolicy = passwordPolicy;
		this.userMapper = userMapper;
		this.events = events;
		this.properties = properties;
		this.clock = clock;
		this.timingDummyHash = passwordEncoder.encode(AccountConstants.TIMING_DUMMY_PASSWORD);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserResponseDTO> findById(UUID userId) {
		return this.repository.findById(userId).map(this.userMapper::toResponse);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserResponseDTO> findByIds(Collection<UUID> userIds) {
		return this.repository.findAllById(userIds).stream().map(this.userMapper::toResponse).toList();
	}

	@Override
	@Transactional(noRollbackFor = AppException.class)
	public UserResponseDTO authenticate(String email, String password) {
		Instant now = this.clock.instant();
		AppUser user = this.repository.findByEmail(email.strip()).orElse(null);
		if (user == null) {
			this.passwordEncoder.matches(password, this.timingDummyHash);
			throw invalidCredentials();
		}
		if (user.isLocked(now)) {
			throw locked(user);
		}
		if (!this.passwordEncoder.matches(password, user.getPasswordHash())) {
			user.recordFailedLogin(now, this.properties.lockoutMaxAttempts(), this.properties.lockoutWindow(),
					this.properties.lockoutDuration());
			throw user.isLocked(now) ? locked(user) : invalidCredentials();
		}
		if (!user.isActive()) {
			throw new AppException(HttpStatus.FORBIDDEN, AccountConstants.ERROR_ACCOUNT_DISABLED,
					AccountConstants.MESSAGE_ACCOUNT_DISABLED);
		}
		if (!user.isEmailVerified()) {
			throw new AppException(HttpStatus.FORBIDDEN, AccountConstants.ERROR_EMAIL_NOT_VERIFIED,
					AccountConstants.MESSAGE_EMAIL_NOT_VERIFIED);
		}
		user.recordSuccessfulLogin(now);
		return this.userMapper.toResponse(user);
	}

	@Override
	@Transactional
	public void signup(SignupCreateDTO request) {
		String email = request.email().strip();
		this.passwordPolicy.check(request.password(), email);
		if (this.repository.existsByEmail(email)) {
			throw emailTaken();
		}
		AppUser user;
		try {
			user = this.repository.saveAndFlush(new AppUser(email, request.fullName().strip(),
					this.passwordEncoder.encode(request.password()), this.clock.instant()));
		}
		catch (DataIntegrityViolationException ex) {
			// Another signup with the same email won the race.
			throw emailTaken();
		}
		this.emailTokenService.issue(user, EmailTokenPurpose.VERIFY_EMAIL);
	}

	@Override
	@Transactional
	public void verifyEmail(String token) {
		UUID userId = this.emailTokenService.redeem(token, EmailTokenPurpose.VERIFY_EMAIL);
		this.repository.findById(userId).orElseThrow().markEmailVerified(this.clock.instant());
	}

	@Override
	@Transactional
	public void resendVerification(String email) {
		this.repository.findByEmail(email.strip())
			.filter((user) -> !user.isEmailVerified())
			.ifPresent((user) -> this.emailTokenService.issue(user, EmailTokenPurpose.VERIFY_EMAIL));
	}

	@Override
	@Transactional
	public void requestPasswordReset(String email) {
		this.repository.findByEmail(email.strip())
			.filter(AppUser::isActive)
			.ifPresent((user) -> this.emailTokenService.issue(user, EmailTokenPurpose.RESET_PASSWORD));
	}

	@Override
	@Transactional
	public void resetPassword(String token, String newPassword) {
		UUID userId = this.emailTokenService.redeem(token, EmailTokenPurpose.RESET_PASSWORD);
		AppUser user = this.repository.findById(userId).orElseThrow();
		this.passwordPolicy.check(newPassword, user.getEmail());
		user.resetPassword(this.passwordEncoder.encode(newPassword), this.clock.instant());
		this.events.publishEvent(new UserPasswordResetEvent(user.getId()));
	}

	@Override
	@Transactional(readOnly = true)
	public MeResponseDTO getMe(UUID userId) {
		AppUser user = this.repository.findById(userId)
			.orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, AccountConstants.ERROR_USER_NOT_FOUND,
					AccountConstants.MESSAGE_USER_NOT_FOUND));
		return this.userMapper.toMe(user, this.portalLookUpService.findMemberships(userId));
	}

	@Override
	@Transactional
	public MeResponseDTO updatePreferences(UUID userId, UserPreferencesUpdateDTO request) {
		String timeZone = (request.timeZone() == null || request.timeZone().isBlank()) ? null
				: request.timeZone().strip();
		if (timeZone != null && !isValidZone(timeZone)) {
			throw new AppException(HttpStatus.BAD_REQUEST, AppConstants.ERROR_VALIDATION_FAILED,
					AppConstants.MESSAGE_VALIDATION_FAILED,
					Map.of(AccountConstants.FIELD_TIME_ZONE, AccountConstants.MESSAGE_TIME_ZONE_INVALID));
		}
		this.repository.findById(userId).orElseThrow().updateTimeZone(timeZone, this.clock.instant());
		return getMe(userId);
	}

	private static boolean isValidZone(String zone) {
		try {
			ZoneId.of(zone);
			return true;
		}
		catch (DateTimeException ex) {
			return false;
		}
	}

	private static AppException emailTaken() {
		return new AppException(HttpStatus.CONFLICT, AccountConstants.ERROR_EMAIL_TAKEN,
				AccountConstants.MESSAGE_EMAIL_TAKEN);
	}

	private static AppException invalidCredentials() {
		return new AppException(HttpStatus.UNAUTHORIZED, AccountConstants.ERROR_INVALID_CREDENTIALS,
				AccountConstants.MESSAGE_INVALID_CREDENTIALS);
	}

	private static AppException locked(AppUser user) {
		return new AppException(HttpStatus.LOCKED, AccountConstants.ERROR_ACCOUNT_LOCKED,
				AccountConstants.MESSAGE_ACCOUNT_LOCKED,
				Map.of(AccountConstants.DETAIL_LOCKED_UNTIL, user.getLockedUntil()));
	}

}
