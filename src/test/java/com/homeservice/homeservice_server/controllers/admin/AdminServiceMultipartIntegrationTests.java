package com.homeservice.homeservice_server.controllers.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeservice.homeservice_server.dto.admin.service.AdminUploadImageResponse;
import com.homeservice.homeservice_server.dto.supabase.SupabaseGetUserResponse;
import com.homeservice.homeservice_server.entities.Category;
import com.homeservice.homeservice_server.entities.User;
import com.homeservice.homeservice_server.enums.UserRole;
import com.homeservice.homeservice_server.repositories.UserRepository;
import com.homeservice.homeservice_server.repositories.admin.AdminCategoryRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminServiceMultipartIntegrationTests {
	private final ObjectMapper objectMapper = new ObjectMapper();

	private MockMvc mockMvc;
	private String adminToken;

	@Autowired
	private AdminCategoryRepository adminCategoryRepository;

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
		this.adminToken = createUserAndToken(UserRole.ADMIN, "admin-service-multipart");
	}

	@Test
	void create_withMultipartImage_uploadsAndStoresBucketUrl() throws Exception {
		Integer categoryId = createCategory("Multipart Create");
		when(adminUploadImageService.uploadServiceImage(any()))
				.thenReturn(new AdminUploadImageResponse("https://project.supabase.co/storage/v1/object/public/admin-service/services/create.png"));

		mockMvc.perform(multipart("/api/admin/services")
						.file(image("create.png"))
						.param("categoryId", categoryId.toString())
						.param("name", uniqueValue("Service Create"))
						.param("subServices", """
								[
								  {"name":"Package A","unit":"unit","pricePerUnit":100},
								  {"name":"Package B","unit":"unit","pricePerUnit":200}
								]
								""")
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.imageUrl")
						.value("https://project.supabase.co/storage/v1/object/public/admin-service/services/create.png"));
	}

	@Test
	void create_withoutImage_returns400() throws Exception {
		Integer categoryId = createCategory("Multipart Missing Image");

		mockMvc.perform(multipart("/api/admin/services")
						.param("categoryId", categoryId.toString())
						.param("name", uniqueValue("Service Create No Image"))
						.param("subServices", """
								[
								  {"name":"Package A","unit":"unit","pricePerUnit":100}
								]
								""")
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Image file is required"));
	}

	@Test
	void create_withImageUrlField_returns400() throws Exception {
		Integer categoryId = createCategory("Multipart Reject ImageUrl");

		mockMvc.perform(multipart("/api/admin/services")
						.file(image("create.png"))
						.param("categoryId", categoryId.toString())
						.param("name", uniqueValue("Service Reject ImageUrl"))
						.param("imageUrl", "https://picsum.photos/600/400")
						.param("subServices", """
								[
								  {"name":"Package A","unit":"unit","pricePerUnit":100}
								]
								""")
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("imageUrl must not be provided"));
	}

	@Test
	void update_withMultipartImage_replacesImageUrl() throws Exception {
		Integer categoryId = createCategory("Multipart Update");
		Integer serviceId = createService(categoryId);
		when(adminUploadImageService.uploadServiceImage(any()))
				.thenReturn(new AdminUploadImageResponse("https://project.supabase.co/storage/v1/object/public/admin-service/services/update.png"));

		mockMvc.perform(putMultipart("/api/admin/services/{serviceId}", serviceId)
						.file(image("update.png"))
						.param("categoryId", categoryId.toString())
						.param("name", uniqueValue("Service Updated"))
						.param("subServices", """
								[
								  {"subServiceId":null,"name":"Package X","unit":"job","pricePerUnit":300}
								]
								""")
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.imageUrl")
						.value("https://project.supabase.co/storage/v1/object/public/admin-service/services/update.png"));
	}

	@Test
	void update_withoutImage_returns400() throws Exception {
		Integer categoryId = createCategory("Multipart Update Missing Image");
		Integer serviceId = createService(categoryId);

		mockMvc.perform(putMultipart("/api/admin/services/{serviceId}", serviceId)
						.param("categoryId", categoryId.toString())
						.param("name", uniqueValue("Service Updated No Image"))
						.param("subServices", """
								[
								  {"subServiceId":null,"name":"Package X","unit":"job","pricePerUnit":300}
								]
								""")
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Image file is required"));
	}

	@Test
	void patch_withMultipartImage_replacesImageUrl() throws Exception {
		Integer categoryId = createCategory("Multipart Patch");
		Integer serviceId = createService(categoryId);
		when(adminUploadImageService.uploadServiceImage(any()))
				.thenReturn(new AdminUploadImageResponse("https://project.supabase.co/storage/v1/object/public/admin-service/services/patch.png"));

		mockMvc.perform(patchMultipart("/api/admin/services/{serviceId}", serviceId)
						.file(image("patch.png"))
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.imageUrl")
						.value("https://project.supabase.co/storage/v1/object/public/admin-service/services/patch.png"));
	}

	@Test
	void patch_withoutImage_stillAllowsOtherFields() throws Exception {
		Integer categoryId = createCategory("Multipart Patch Name");
		Integer serviceId = createService(categoryId);

		mockMvc.perform(patchMultipart("/api/admin/services/{serviceId}", serviceId)
						.param("name", uniqueValue("Patched Name"))
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value(org.hamcrest.Matchers.startsWith("Patched Name")));
	}

	@Test
	void patch_withMultipartImageUrl_updatesImageUrl() throws Exception {
		Integer categoryId = createCategory("Multipart Patch ImageUrl");
		Integer serviceId = createService(categoryId);

		mockMvc.perform(patchMultipart("/api/admin/services/{serviceId}", serviceId)
						.param("imageUrl", "https://picsum.photos/600/400")
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.imageUrl").value("https://picsum.photos/600/400"));
	}

	@Test
	void patch_withJsonImageUrl_updatesImageUrl() throws Exception {
		Integer categoryId = createCategory("Json Patch ImageUrl");
		Integer serviceId = createService(categoryId);

		mockMvc.perform(patch("/api/admin/services/{serviceId}", serviceId)
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
						.content("""
								{
								  "imageUrl": "https://picsum.photos/600/400"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.imageUrl").value("https://picsum.photos/600/400"));
	}

	@Test
	void patch_withImageAndImageUrlTogether_returns400() throws Exception {
		Integer categoryId = createCategory("Multipart Patch Both Image Sources");
		Integer serviceId = createService(categoryId);

		mockMvc.perform(patchMultipart("/api/admin/services/{serviceId}", serviceId)
						.file(image("patch.png"))
						.param("imageUrl", "https://picsum.photos/600/400")
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("image and imageUrl cannot be provided together"));
	}

	private Integer createCategory(String prefix) {
		int nextSortOrder = (int) adminCategoryRepository.count() + 1;
		Category category = Category.builder()
				.name(uniqueValue(prefix))
				.sortOrder(nextSortOrder)
				.build();
		return adminCategoryRepository.save(category).getCategoryId();
	}

	private Integer createService(Integer categoryId) throws Exception {
		when(adminUploadImageService.uploadServiceImage(any()))
				.thenReturn(new AdminUploadImageResponse("https://project.supabase.co/storage/v1/object/public/admin-service/services/original.png"));

		MvcResult result = mockMvc.perform(multipart("/api/admin/services")
						.file(image("original.png"))
						.param("categoryId", categoryId.toString())
						.param("name", uniqueValue("Base Service"))
						.param("subServices", """
								[
								  {"name":"Package A","unit":"unit","pricePerUnit":100}
								]
								""")
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isCreated())
				.andReturn();

		JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
		return node.get("serviceId").asInt();
	}

	private MockMultipartFile image(String filename) {
		return new MockMultipartFile("image", filename, "image/png", new byte[]{1, 2, 3});
	}

	private MockMultipartHttpServletRequestBuilder putMultipart(String urlTemplate, Object... uriVariables) {
		MockMultipartHttpServletRequestBuilder builder = multipart(urlTemplate, uriVariables);
		builder.with(request -> {
			request.setMethod("PUT");
			return request;
		});
		return builder;
	}

	private MockMultipartHttpServletRequestBuilder patchMultipart(String urlTemplate, Object... uriVariables) {
		MockMultipartHttpServletRequestBuilder builder = multipart(urlTemplate, uriVariables);
		builder.with(request -> {
			request.setMethod("PATCH");
			return request;
		});
		return builder;
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

	private String uniqueValue(String prefix) {
		return prefix + "-" + System.nanoTime();
	}
}
