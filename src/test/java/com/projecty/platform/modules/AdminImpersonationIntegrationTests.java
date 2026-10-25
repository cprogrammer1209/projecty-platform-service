package com.projecty.platform.modules;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.Cookie;

import com.jayway.jsonpath.JsonPath;
import com.projecty.platform.TestcontainersConfiguration;
import com.projecty.platform.modules.account.internal.entity.EmailTokenPurpose;
import com.projecty.platform.modules.account.internal.event.EmailTokenIssuedEvent;
import com.projecty.platform.modules.admin.internal.constants.AdminConstants;
import com.projecty.platform.modules.admin.internal.entity.AdminUser;
import com.projecty.platform.modules.admin.internal.repository.AdminUserRepository;
import com.projecty.platform.modules.session.internal.constants.SessionConstants;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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
 * Admin console sign-in, tenant views and support impersonation, end to end against a real Postgres. The
 * first admin comes from the dev bootstrap settings in application.yml.
 */
@SpringBootTest
@AutoConfigureMockMvc
@RecordApplicationEvents
@Import({ TestcontainersConfiguration.class, AuthFlowIntegrationTests.CapturingMail.class })
class AdminImpersonationIntegrationTests {

	private static final String ADMIN_EMAIL = "admin@projecty.local";

	private static final String ADMIN_PASSWORD = "projecty-admin-dev";

	private static final String PASSWORD = "correct-horse-battery";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ApplicationEvents events;

	@Autowired
	private AdminUserRepository adminUserRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void adminSignsInAndTokensOnlyWorkOnTheirOwnSide() throws Exception {
		Session admin = adminLogin(ADMIN_EMAIL, ADMIN_PASSWORD);
		this.mvc.perform(get("/api/admin/auth/me").header("Authorization", admin.bearer()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.email").value(ADMIN_EMAIL));
		assertThat(claims(admin.accessToken())).contains("projecty-admin").contains("PLATFORM_ADMIN");

		Customer customer = customerWithPortal();
		this.mvc.perform(get("/api/me").header("Authorization", admin.bearer())).andExpect(status().isForbidden());
		this.mvc.perform(get("/api/admin/tenants").header("Authorization", customer.session().bearer()))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
		this.mvc.perform(get("/api/admin/tenants")).andExpect(status().isUnauthorized());

		// A customer can't sign in to the admin console with their own password.
		this.mvc.perform(json(post("/api/admin/auth/login"),
				"{\"email\":\"%s\",\"password\":\"%s\"}".formatted(customer.email(), PASSWORD)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));

		Session refreshed = session(this.mvc.perform(post("/api/admin/auth/refresh").cookie(admin.refreshCookie()))
			.andExpect(status().isOk())
			.andReturn(), AdminConstants.REFRESH_COOKIE_NAME);
		this.mvc.perform(post("/api/admin/auth/refresh").cookie(admin.refreshCookie()))
			.andExpect(status().isUnauthorized());

		this.mvc.perform(post("/api/admin/auth/logout").cookie(refreshed.refreshCookie())).andExpect(status().isOk());
		this.mvc.perform(post("/api/admin/auth/refresh").cookie(refreshed.refreshCookie()))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("SESSION_EXPIRED"));
	}

	@Test
	void fiveFailedAdminLoginsLockTheAccount() throws Exception {
		String email = "locked-" + UUID.randomUUID() + "@projecty.local";
		this.adminUserRepository
			.save(new AdminUser(email, "Locked Admin", this.passwordEncoder.encode(ADMIN_PASSWORD), Instant.now()));
		for (int i = 0; i < 4; i++) {
			adminLoginRequest(email, "wrong-password-1").andExpect(status().isUnauthorized());
		}
		adminLoginRequest(email, "wrong-password-1").andExpect(status().isLocked());
		adminLoginRequest(email, ADMIN_PASSWORD).andExpect(status().isLocked())
			.andExpect(jsonPath("$.error.code").value("ACCOUNT_LOCKED"));
	}

	@Test
	void adminSeesTenantsAndTheirUsers() throws Exception {
		Session admin = adminLogin(ADMIN_EMAIL, ADMIN_PASSWORD);
		Customer customer = customerWithPortal();

		MvcResult tenants = this.mvc.perform(get("/api/admin/tenants").header("Authorization", admin.bearer()))
			.andExpect(status().isOk())
			.andReturn();
		List<String> owners = JsonPath.read(tenants.getResponse().getContentAsString(),
				"$.data[?(@.id == '%s')].ownerEmail".formatted(customer.portalId()));
		assertThat(owners).containsExactly(customer.email());

		this.mvc.perform(get("/api/admin/tenants/" + customer.portalId()).header("Authorization", admin.bearer()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.userCount").value(1))
			.andExpect(jsonPath("$.data.activeUserCount").value(1))
			.andExpect(jsonPath("$.data.lastActivityAt").exists());
		this.mvc.perform(get("/api/admin/tenants/" + customer.portalId() + "/users").header("Authorization", admin.bearer()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].email").value(customer.email()))
			.andExpect(jsonPath("$.data[0].profile").value("PORTAL_OWNER"))
			.andExpect(jsonPath("$.data[0].active").value(true));
		this.mvc.perform(get("/api/admin/tenants/" + UUID.randomUUID()).header("Authorization", admin.bearer()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("TENANT_NOT_FOUND"));
	}

	@Test
	void impersonationRequestsAreValidated() throws Exception {
		Session admin = adminLogin(ADMIN_EMAIL, ADMIN_PASSWORD);
		Customer customer = customerWithPortal();
		Customer stranger = customerWithPortal();

		startImpersonation(admin, customer.portalId(), customer.userId(), 30, "Demo", true)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("IMPERSONATION_INVALID"));
		startImpersonation(admin, customer.portalId(), customer.userId(), 15, "Curiosity", true)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("IMPERSONATION_INVALID"));
		startImpersonation(admin, customer.portalId(), customer.userId(), 15, "Demo", false)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
			.andExpect(jsonPath("$.error.details.acknowledged").exists());
		startImpersonation(admin, customer.portalId(), stranger.userId(), 15, "Demo", true)
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.error.code").value("IMPERSONATION_TARGET_NOT_ELIGIBLE"));
		startImpersonation(customer.session(), customer.portalId(), customer.userId(), 15, "Demo", true)
			.andExpect(status().isForbidden());
	}

	@Test
	void supportSessionActsAsTheUserUntilEnded() throws Exception {
		Session admin = adminLogin(ADMIN_EMAIL, ADMIN_PASSWORD);
		String adminId = JsonPath.read(claims(admin.accessToken()), "$.sub");
		Customer customer = customerWithPortal();

		MvcResult started = startImpersonation(admin, customer.portalId(), customer.userId(), 60, "Configuration", true)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.launchUrl").value(org.hamcrest.Matchers.startsWith("http://localhost:4200/impersonate?code=")))
			.andReturn();
		String launchUrl = JsonPath.read(started.getResponse().getContentAsString(), "$.data.launchUrl");
		String code = launchUrl.substring(launchUrl.indexOf("code=") + 5);

		Session support = session(redeem(code).andExpect(status().isOk()).andReturn(), SessionConstants.REFRESH_COOKIE_NAME);
		String tokenClaims = claims(support.accessToken());
		assertThat((String) JsonPath.read(tokenClaims, "$.sub")).isEqualTo(customer.userId());
		assertThat((String) JsonPath.read(tokenClaims, "$.act.sub")).isEqualTo(adminId);
		assertThat((String) JsonPath.read(tokenClaims, "$.act.name")).isEqualTo("Platform Admin");
		assertThat(tokenClaims).contains("\"imp\"");

		this.mvc.perform(get("/api/me").header("Authorization", support.bearer()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.email").value(customer.email()));
		redeem(code).andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("IMPERSONATION_LINK_INVALID"));

		Session refreshed = session(this.mvc.perform(post("/api/auth/refresh").cookie(support.refreshCookie()))
			.andExpect(status().isOk())
			.andReturn(), SessionConstants.REFRESH_COOKIE_NAME);
		assertThat((String) JsonPath.read(claims(refreshed.accessToken()), "$.act.sub")).isEqualTo(adminId);

		// The customer's own session is not a support session.
		this.mvc.perform(post("/api/auth/impersonation/end").cookie(customer.session().refreshCookie()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("NOT_IMPERSONATING"));

		this.mvc.perform(post("/api/auth/impersonation/end").cookie(refreshed.refreshCookie())).andExpect(status().isOk());
		this.mvc.perform(post("/api/auth/refresh").cookie(refreshed.refreshCookie())).andExpect(status().isUnauthorized());

		MvcResult audit = this.mvc
			.perform(get("/api/admin/audit-events").param("portalId", customer.portalId())
				.header("Authorization", admin.bearer()))
			.andExpect(status().isOk())
			.andReturn();
		List<String> actions = JsonPath.read(audit.getResponse().getContentAsString(), "$.data[*].action");
		assertThat(actions).containsExactly("IMPERSONATION_ENDED", "IMPERSONATION_STARTED", "IMPERSONATION_REQUESTED");
		String requestDetail = JsonPath.read(audit.getResponse().getContentAsString(), "$.data[2].detail");
		assertThat(requestDetail).contains("Reason: Configuration").contains(customer.email());

		this.mvc.perform(get("/api/admin/audit-events").header("Authorization", admin.bearer()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[?(@.action == 'ADMIN_SIGNED_IN')]").isNotEmpty());
	}

	private Session adminLogin(String email, String password) throws Exception {
		return session(adminLoginRequest(email, password).andExpect(status().isOk()).andReturn(),
				AdminConstants.REFRESH_COOKIE_NAME);
	}

	private ResultActions adminLoginRequest(String email, String password) throws Exception {
		return this.mvc.perform(json(post("/api/admin/auth/login"),
				"{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)));
	}

	private ResultActions startImpersonation(Session admin, String portalId, String userId, int minutes, String reason,
			boolean acknowledged) throws Exception {
		return this.mvc.perform(json(post("/api/admin/impersonations").header("Authorization", admin.bearer()), """
				{"portalId":"%s","targetUserId":"%s","durationMinutes":%d,"reason":"%s",
				 "comment":"Customer asked for help with settings","acknowledged":%s}"""
			.formatted(portalId, userId, minutes, reason, acknowledged)));
	}

	private ResultActions redeem(String code) throws Exception {
		return this.mvc.perform(json(post("/api/auth/impersonation/redeem"), "{\"code\":\"%s\"}".formatted(code)));
	}

	/**
	 * A verified customer who owns a new portal.
	 */
	private Customer customerWithPortal() throws Exception {
		String email = "user-" + UUID.randomUUID() + "@example.com";
		this.mvc.perform(json(post("/api/auth/signup"),
				"{\"fullName\":\"Test User\",\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, PASSWORD)))
			.andExpect(status().isCreated());
		String token = this.events.stream(EmailTokenIssuedEvent.class)
			.filter((event) -> event.email().equals(email) && event.purpose() == EmailTokenPurpose.VERIFY_EMAIL)
			.findFirst()
			.orElseThrow()
			.token();
		this.mvc.perform(json(post("/api/auth/verify-email"), "{\"token\":\"%s\"}".formatted(token)))
			.andExpect(status().isOk());
		Session session = session(this.mvc
			.perform(json(post("/api/auth/login"), "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, PASSWORD)))
			.andExpect(status().isOk())
			.andReturn(), SessionConstants.REFRESH_COOKIE_NAME);
		MvcResult portal = this.mvc
			.perform(json(post("/api/portals").header("Authorization", session.bearer()),
					"{\"name\":\"Portal\",\"slug\":\"p-%s\"}".formatted(UUID.randomUUID().toString().substring(0, 8))))
			.andExpect(status().isCreated())
			.andReturn();
		String portalId = JsonPath.read(portal.getResponse().getContentAsString(), "$.data.id");
		String userId = JsonPath.read(claims(session.accessToken()), "$.sub");
		return new Customer(email, userId, portalId, session);
	}

	private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
		return request.contentType(MediaType.APPLICATION_JSON).content(body);
	}

	private static Session session(MvcResult result, String cookieName) throws Exception {
		String accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
		Cookie cookie = result.getResponse().getCookie(cookieName);
		assertThat(cookie).isNotNull();
		assertThat(cookie.isHttpOnly()).isTrue();
		return new Session(accessToken, cookie);
	}

	private static String claims(String jwt) {
		return new String(Base64.getUrlDecoder().decode(jwt.split("\\.")[1]));
	}

	record Session(String accessToken, Cookie refreshCookie) {

		String bearer() {
			return "Bearer " + this.accessToken;
		}

	}

	record Customer(String email, String userId, String portalId, Session session) {
	}

}
