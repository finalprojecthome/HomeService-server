package com.homeservice.homeservice_server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeservice.homeservice_server.dto.AdminLoginRequest;
import com.homeservice.homeservice_server.dto.AdminRegisterRequest;
import com.homeservice.homeservice_server.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminAuthIntegrationTests {
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@org.springframework.beans.factory.annotation.Autowired
	private UserRepository userRepository;

	@Value("${jwt.secret}")
	private String jwtSecret;

	@org.springframework.beans.factory.annotation.Autowired
	void setUp(WebApplicationContext context, FilterChainProxy springSecurityFilterChain) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.addFilters(springSecurityFilterChain)
				.build();
	}

	@Test
	void register_wrongInvite_returns403() throws Exception {
		var payload = new AdminRegisterRequest(
				"Admin",
				"0999999999",
				"admin1@example.com",
				"password123",
				"wrong"
		);

		mockMvc.perform(post("/api/admin/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(payload)))
				.andExpect(status().isForbidden());
	}

	@Test
	void register_then_accessProtectedEndpoint_withToken_ok() throws Exception {
		var payload = new AdminRegisterRequest(
				"Admin",
				"0999999999",
				"admin2@example.com",
				"password123",
				"test-invite"
		);

		String token = mockMvc.perform(post("/api/admin/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(payload)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isString())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String accessToken = objectMapper.readTree(token).get("accessToken").asText();

		mockMvc.perform(get("/api/admin/auth/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("admin"));

		Claims claims = parseClaims(accessToken);
		org.junit.jupiter.api.Assertions.assertEquals("admin", claims.get("role"));
		org.junit.jupiter.api.Assertions.assertEquals(
				"admin",
				userRepository.findRawRoleByEmail("admin2@example.com")
		);
	}

	@Test
	void login_wrongPassword_returns401() throws Exception {
		var registerPayload = new AdminRegisterRequest(
				"Admin",
				"0999999999",
				"admin3@example.com",
				"password123",
				"test-invite"
		);

		mockMvc.perform(post("/api/admin/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerPayload)))
				.andExpect(status().isOk());

		var loginPayload = new AdminLoginRequest("admin3@example.com", "wrongpass123");

		mockMvc.perform(post("/api/admin/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginPayload)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void protectedEndpoint_withoutToken_returns401() throws Exception {
		mockMvc.perform(get("/api/admin/auth/me"))
				.andExpect(status().isUnauthorized());
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(buildKey(jwtSecret))
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private SecretKey buildKey(String secret) {
		try {
			return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		} catch (IllegalArgumentException ignored) {
			return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		}
	}
}
