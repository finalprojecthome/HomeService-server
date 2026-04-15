package com.homeservice.homeservice_server.controllers.admin;

import com.homeservice.homeservice_server.dto.admin.service.AdminUploadImageResponse;
import com.homeservice.homeservice_server.dto.supabase.SupabaseGetUserResponse;
import com.homeservice.homeservice_server.entities.User;
import com.homeservice.homeservice_server.enums.UserRole;
import com.homeservice.homeservice_server.repositories.UserRepository;
import com.homeservice.homeservice_server.services.SupabaseAuthClient;
import com.homeservice.homeservice_server.services.admin.AdminUploadImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminUploadImageIntegrationTests {
	private MockMvc mockMvc;
	private String adminToken;

	@Autowired
	private UserRepository userRepository;

	@MockitoBean
	private SupabaseAuthClient supabaseAuthClient;

	@MockitoBean
	private AdminUploadImageService adminUploadImageService;

	@Autowired
	void setUp(WebApplicationContext context, FilterChainProxy springSecurityFilterChain) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.addFilters(springSecurityFilterChain)
				.build();
	}

	@BeforeEach
	void resetData() {
		this.adminToken = createUserAndToken(UserRole.ADMIN, "admin-upload");
	}

	@Test
	void uploadImage_withoutToken_returns401() throws Exception {
		mockMvc.perform(multipart("/api/admin/services/upload-image")
						.file(new MockMultipartFile("image", "service.png", "image/png", new byte[]{1, 2, 3})))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void uploadImage_withNonAdminToken_returns403() throws Exception {
		String userToken = createUserAndToken(UserRole.CUSTOMER, "normal-upload");

		mockMvc.perform(multipart("/api/admin/services/upload-image")
						.file(new MockMultipartFile("image", "service.png", "image/png", new byte[]{1, 2, 3}))
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isForbidden());
	}

	@Test
	void uploadImage_withAdminToken_returnsImageUrl() throws Exception {
		when(adminUploadImageService.uploadServiceImage(any()))
				.thenReturn(new AdminUploadImageResponse("https://project.supabase.co/storage/v1/object/public/admin-service/services/test.png"));

		mockMvc.perform(multipart("/api/admin/services/upload-image")
						.file(new MockMultipartFile("image", "service.png", "image/png", new byte[]{1, 2, 3}))
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.imageUrl")
						.value("https://project.supabase.co/storage/v1/object/public/admin-service/services/test.png"));
	}

	private String createUserAndToken(UserRole role, String prefix) {
		UUID userId = UUID.randomUUID();
		String email = prefix + "-" + System.nanoTime() + "@example.com";
		String token = prefix + "-token-" + System.nanoTime();

		userRepository.save(User.builder()
				.userId(userId)
				.name("Test User")
				.phone(uniquePhone())
				.email(email)
				.role(role)
				.build());

		when(supabaseAuthClient.getUser(token))
				.thenReturn(new SupabaseGetUserResponse(userId.toString(), email, null));

		return token;
	}

	private String bearer() {
		return "Bearer " + adminToken;
	}

	private String uniquePhone() {
		long suffix = Math.floorMod(System.nanoTime(), 100_000_000L);
		return String.format("09%08d", suffix);
	}
}
