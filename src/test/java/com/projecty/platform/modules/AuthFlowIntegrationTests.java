package com.projecty.platform.modules;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.Cookie;

import com.jayway.jsonpath.JsonPath;
import com.projecty.platform.TestcontainersConfiguration;
import com.projecty.platform.external.mail.dto.EmailMessageDTO;
import com.projecty.platform.external.mail.service.MailService;
import com.projecty.platform.modules.account.internal.entity.EmailTokenPurpose;
import com.projecty.platform.modules.account.internal.event.EmailTokenIssuedEvent;
import com.projecty.platform.modules.session.internal.constants.SessionConstants;
import com.projecty.platform.modules.token.internal.service.SigningKeyService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end auth flows against a real Postgres (Testcontainers). Email links are read from the published
 * {@link EmailTokenIssuedEvent} events; emails are captured at the {@link MailService} boundary instead of sent.
 */
@SpringBootTest(properties = "projecty.portal.max-portals-per-user=2")
@AutoConfigureMockMvc
@RecordApplicationEvents
@Import({ TestcontainersConfiguration.class, AuthFlowIntegrationTests.CapturingMail.class })
class AuthFlowIntegrationTests {

	private static final String PASSWORD = "correct-horse-battery";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ApplicationEvents events;

	@Autowired
	private CapturingMailService mailService;

	@Autowired
	private JwtEncoder jwtEncoder;

	@Autowired
	private SigningKeyService signingKeys;

	@Test
	void signupVerifyLoginCreatePortalAndRefresh() throws Exception {
		String email = newEmail();
		signup(email, PASSWORD).andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true));
		assertThat(this.mailService.sentTo(email)).isTrue();

		login(email, PASSWORD).andExpect(status().isForbidden()).andExpect(jsonPath("$.error.code").value("EMAIL_NOT_VERIFIED"));

		verify(email);
		Session session = loggedIn(email, PASSWORD);

		this.mvc.perform(get("/api/me").header("Authorization", session.bearer()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.email").value(email))
			.andExpect(jsonPath("$.data.portals").isEmpty());

		String slug = "acme-" + UUID.randomUUID().toString().substring(0, 8);
		MvcResult created = this.mvc
			.perform(post("/api/portals").header("Authorization", session.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Acme\",\"slug\":\"%s\"}".formatted(slug)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.profile").value("PORTAL_OWNER"))
			.andReturn();
		String portalId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

		Session refreshed = refresh(session.refreshCookie());
		assertThat(claims(refreshed.accessToken())).contains(portalId);

		this.mvc
			.perform(post("/api/portals").header("Authorization", refreshed.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Copy\",\"slug\":\"%s\"}".formatted(slug)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("SLUG_TAKEN"))
			.andExpect(jsonPath("$.error.details.suggestions").isNotEmpty());
	}

	@Test
	void weakPasswordAndDuplicateEmailAreRejected() throws Exception {
		String email = newEmail();
		signup(email, "password123").andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("WEAK_PASSWORD"));
		assertThat(this.events.stream(EmailTokenIssuedEvent.class)).isEmpty();

		signup(email, PASSWORD).andExpect(status().isCreated());
		signup(email.toUpperCase(), PASSWORD).andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("EMAIL_TAKEN"));
	}

	@Test
	void fiveFailedLoginsLockTheAccount() throws Exception {
		String email = verifiedUser();
		for (int i = 0; i < 4; i++) {
			login(email, "wrong-password-1").andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
		}
		login(email, "wrong-password-1").andExpect(status().isLocked());
		login(email, PASSWORD).andExpect(status().isLocked()).andExpect(jsonPath("$.error.code").value("ACCOUNT_LOCKED"));
	}

	@Test
	void unknownEmailLooksLikeWrongPassword() throws Exception {
		login(newEmail(), PASSWORD).andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
		this.mvc.perform(json(post("/api/auth/forgot-password"), "{\"email\":\"%s\"}".formatted(newEmail())))
			.andExpect(status().isAccepted());
		assertThat(this.events.stream(EmailTokenIssuedEvent.class)).isEmpty();
	}

	@Test
	void reusingARefreshTokenRevokesTheSession() throws Exception {
		Session session = loggedIn(verifiedUser(), PASSWORD);
		Session rotated = refresh(session.refreshCookie());

		this.mvc.perform(post("/api/auth/refresh").cookie(session.refreshCookie()))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("SESSION_EXPIRED"));
		this.mvc.perform(post("/api/auth/refresh").cookie(rotated.refreshCookie())).andExpect(status().isUnauthorized());
	}

	@Test
	void logoutEndsTheSession() throws Exception {
		Session session = loggedIn(verifiedUser(), PASSWORD);
		MvcResult result = this.mvc.perform(post("/api/auth/logout").cookie(session.refreshCookie()))
			.andExpect(status().isOk())
			.andReturn();
		assertThat(result.getResponse().getCookie(SessionConstants.REFRESH_COOKIE_NAME).getMaxAge()).isZero();
		this.mvc.perform(post("/api/auth/refresh").cookie(session.refreshCookie())).andExpect(status().isUnauthorized());
	}

	@Test
	void passwordResetRevokesAllSessions() throws Exception {
		String email = verifiedUser();
		Session session = loggedIn(email, PASSWORD);

		this.mvc.perform(json(post("/api/auth/forgot-password"), "{\"email\":\"%s\"}".formatted(email)))
			.andExpect(status().isAccepted());
		String token = lastToken(email, EmailTokenPurpose.RESET_PASSWORD);
		String newPassword = "a-brand-new-passphrase";
		this.mvc
			.perform(json(post("/api/auth/reset-password"),
					"{\"token\":\"%s\",\"newPassword\":\"%s\"}".formatted(token, newPassword)))
			.andExpect(status().isOk());

		this.mvc.perform(post("/api/auth/refresh").cookie(session.refreshCookie())).andExpect(status().isUnauthorized());
		login(email, PASSWORD).andExpect(status().isUnauthorized());
		login(email, newPassword).andExpect(status().isOk());
	}

	@Test
	void portalRulesAreEnforced() throws Exception {
		Session session = loggedIn(verifiedUser(), PASSWORD);
		createPortal(session, "-bad-").andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("SLUG_INVALID"));
		createPortal(session, "api").andExpect(status().isBadRequest());
		createPortal(session, uniqueSlug()).andExpect(status().isCreated());
		createPortal(session, uniqueSlug()).andExpect(status().isCreated());
		createPortal(session, uniqueSlug()).andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.error.code").value("PORTAL_LIMIT"));
	}

	@Test
	void invalidAccessTokensAreRejected() throws Exception {
		Session session = loggedIn(verifiedUser(), PASSWORD);
		this.mvc.perform(get("/api/me").header("Authorization", session.bearer())).andExpect(status().isOk());

		this.mvc.perform(get("/api/me"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
		this.mvc.perform(get("/api/me").header("Authorization", session.bearer() + "x"))
			.andExpect(status().isUnauthorized());

		Instant now = Instant.now();
		String subject = JsonPath.read(claims(session.accessToken()), "$.sub");
		JwtClaimsSet.Builder valid = JwtClaimsSet.builder()
			.issuer("http://localhost:8081")
			.audience(List.of("projecty-api"))
			.subject(subject)
			.issuedAt(now)
			.expiresAt(now.plusSeconds(300));
		this.mvc.perform(get("/api/me").header("Authorization", "Bearer " + sign(valid.build())))
			.andExpect(status().isOk());

		String expired = sign(valid.issuedAt(now.minusSeconds(3600)).expiresAt(now.minusSeconds(1800)).build());
		String wrongIssuer = sign(valid.issuedAt(now).expiresAt(now.plusSeconds(300)).issuer("https://evil.example").build());
		String wrongAudience = sign(valid.issuer("http://localhost:8081").audience(List.of("other-api")).build());
		for (String token : List.of(expired, wrongIssuer, wrongAudience)) {
			this.mvc.perform(get("/api/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isUnauthorized());
		}
	}

	@Test
	void jwksPublishesOnlyPublicKeys() throws Exception {
		this.mvc.perform(get("/.well-known/jwks.json"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.keys[0].kid").value(this.signingKeys.signingKey().getKeyID()))
			.andExpect(jsonPath("$.keys[0].n").exists())
			.andExpect(jsonPath("$.keys[0].d").doesNotExist());
	}

	private String verifiedUser() throws Exception {
		String email = newEmail();
		signup(email, PASSWORD).andExpect(status().isCreated());
		verify(email);
		return email;
	}

	private void verify(String email) throws Exception {
		String token = lastToken(email, EmailTokenPurpose.VERIFY_EMAIL);
		this.mvc.perform(json(post("/api/auth/verify-email"), "{\"token\":\"%s\"}".formatted(token)))
			.andExpect(status().isOk());
	}

	private Session loggedIn(String email, String password) throws Exception {
		return session(login(email, password).andExpect(status().isOk()).andReturn());
	}

	private Session refresh(Cookie cookie) throws Exception {
		return session(this.mvc.perform(post("/api/auth/refresh").cookie(cookie)).andExpect(status().isOk()).andReturn());
	}

	private ResultActions signup(String email, String password) throws Exception {
		return this.mvc.perform(json(post("/api/auth/signup"),
				"{\"fullName\":\"Test User\",\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)));
	}

	private ResultActions login(String email, String password) throws Exception {
		return this.mvc.perform(
				json(post("/api/auth/login"), "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)));
	}

	private ResultActions createPortal(Session session, String slug) throws Exception {
		return this.mvc.perform(json(post("/api/portals").header("Authorization", session.bearer()),
				"{\"name\":\"Portal\",\"slug\":\"%s\"}".formatted(slug)));
	}

	private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
		return request.contentType(MediaType.APPLICATION_JSON).content(body);
	}

	private String lastToken(String email, EmailTokenPurpose purpose) {
		return this.events.stream(EmailTokenIssuedEvent.class)
			.filter((event) -> event.email().equals(email) && event.purpose() == purpose)
			.reduce((first, second) -> second)
			.orElseThrow()
			.token();
	}

	private String sign(JwtClaimsSet claims) {
		JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(this.signingKeys.signingKey().getKeyID()).build();
		return this.jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	private static Session session(MvcResult result) throws Exception {
		String accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
		Cookie cookie = result.getResponse().getCookie(SessionConstants.REFRESH_COOKIE_NAME);
		assertThat(cookie).isNotNull();
		assertThat(cookie.isHttpOnly()).isTrue();
		return new Session(accessToken, cookie);
	}

	private static String claims(String jwt) {
		return new String(Base64.getUrlDecoder().decode(jwt.split("\\.")[1]));
	}

	private static String newEmail() {
		return "user-" + UUID.randomUUID() + "@example.com";
	}

	private static String uniqueSlug() {
		return "p-" + UUID.randomUUID().toString().substring(0, 8);
	}

	record Session(String accessToken, Cookie refreshCookie) {

		String bearer() {
			return "Bearer " + this.accessToken;
		}

	}

	@TestConfiguration(proxyBeanMethods = false)
	static class CapturingMail {

		@Bean
		@Primary
		CapturingMailService capturingMailService() {
			return new CapturingMailService();
		}

	}

	/**
	 * Keeps emails in memory instead of sending them.
	 */
	static class CapturingMailService implements MailService {

		private final List<EmailMessageDTO> sent = new ArrayList<>();

		@Override
		public void send(EmailMessageDTO message) {
			this.sent.add(message);
		}

		boolean sentTo(String email) {
			return this.sent.stream().anyMatch((message) -> message.to().equals(email));
		}

	}

}
