package com.homeservice.homeservice_server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeservice.homeservice_server.dto.AdminLoginRequest;
import com.homeservice.homeservice_server.dto.AdminRegisterRequest;
import com.homeservice.homeservice_server.dto.supabase.SupabaseGetUserResponse;
import com.homeservice.homeservice_server.dto.supabase.SupabaseLoginResponse;
import com.homeservice.homeservice_server.repositories.UserRepository;
import com.homeservice.homeservice_server.services.SupabaseAuthClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminAuthIntegrationTests {
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private UserRepository userRepository;

	@MockitoBean
	private SupabaseAuthClient supabaseAuthClient;

	@BeforeEach
	void resetData() {
		userRepository.deleteAll();
	}

	@Autowired
	void setUp(WebApplicationContext context, FilterChainProxy springSecurityFilterChain) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.addFilters(springSecurityFilterChain)
				.build();
	}

	@Test
	void register_wrongInvite_returns403() throws Exception {
		String email = "admin1@example.com";
		when(supabaseAuthClient.signUp(email, "password123")).thenReturn(UUID.randomUUID());

		var payload = new AdminRegisterRequest(
				"Admin",
				uniquePhone(),
				email,
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
		UUID userId = UUID.randomUUID();
		String email = "admin2@example.com";
		String token = "supabase-token-1";

		when(supabaseAuthClient.signUp(email, "password123")).thenReturn(userId);
		when(supabaseAuthClient.signIn(email, "password123"))
				.thenReturn(new SupabaseLoginResponse(token, "refresh-1", "bearer", 3600L));
		when(supabaseAuthClient.getUser(token))
				.thenReturn(new SupabaseGetUserResponse(userId.toString(), email, null));

		var payload = new AdminRegisterRequest(
				"Admin",
				uniquePhone(),
				email,
				"password123",
				"test-invite"
		);

		mockMvc.perform(post("/api/admin/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(payload)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value(token))
				.andExpect(jsonPath("$.tokenType").value("bearer"));

		mockMvc.perform(get("/api/admin/auth/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("admin"));

		org.junit.jupiter.api.Assertions.assertEquals("admin", userRepository.findRawRoleByEmail(email));
	}

	@Test
	void login_wrongPassword_returns401() throws Exception {
		String email = "admin3@example.com";
		UUID userId = UUID.randomUUID();
		when(supabaseAuthClient.signUp(email, "password123")).thenReturn(userId);
		when(supabaseAuthClient.signIn(email, "password123"))
				.thenReturn(new SupabaseLoginResponse("token-ok", "refresh-ok", "bearer", 3600L));
		when(supabaseAuthClient.getUser("token-ok"))
				.thenReturn(new SupabaseGetUserResponse(userId.toString(), email, null));

		mockMvc.perform(post("/api/admin/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new AdminRegisterRequest(
								"Admin",
								uniquePhone(),
								email,
								"password123",
								"test-invite"
						))))
				.andExpect(status().isOk());

		when(supabaseAuthClient.signIn(email, "wrongpass123"))
				.thenThrow(new com.homeservice.homeservice_server.exception.UnauthorizedException("Invalid credentials"));

		mockMvc.perform(post("/api/admin/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new AdminLoginRequest(email, "wrongpass123"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void protectedEndpoint_withoutToken_returns401() throws Exception {
		mockMvc.perform(get("/api/admin/auth/me"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void protectedEndpoint_withUnknownUser_returns403() throws Exception {
		when(supabaseAuthClient.getUser(anyString()))
				.thenReturn(new SupabaseGetUserResponse(UUID.randomUUID().toString(), "unknown@example.com", null));

		mockMvc.perform(get("/api/admin/auth/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer unknown-token"))
				.andExpect(status().isForbidden());
	}

	private String uniquePhone() {
		long suffix = Math.floorMod(System.nanoTime(), 100_000_000L);
		return String.format("09%08d", suffix);
	}
}
