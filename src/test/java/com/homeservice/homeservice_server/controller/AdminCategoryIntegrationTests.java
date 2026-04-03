package com.homeservice.homeservice_server.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeservice.homeservice_server.dto.AdminRegisterRequest;
import com.homeservice.homeservice_server.entity.ServiceItem;
import com.homeservice.homeservice_server.repository.AdminCategoryRepository;
import com.homeservice.homeservice_server.repository.ServiceItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminCategoryIntegrationTests {
	private MockMvc mockMvc;
	private String adminToken;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private AdminCategoryRepository adminCategoryRepository;

	@Autowired
	private ServiceItemRepository serviceItemRepository;

	@Autowired
	void setUp(WebApplicationContext context, FilterChainProxy springSecurityFilterChain) throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.addFilters(springSecurityFilterChain)
				.build();
		this.adminToken = registerAdminAndGetToken();
	}

	@BeforeEach
	void resetData() {
		serviceItemRepository.deleteAll();
		adminCategoryRepository.deleteAll();
	}

	@Test
	void categories_withoutToken_returns401() throws Exception {
		mockMvc.perform(get("/api/admin/categories"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void createAndListCategories_withPaginationAndSearch_works() throws Exception {
		for (int i = 1; i <= 12; i++) {
			createCategory("Category " + i);
		}
		createCategory("บริการทั่วไป");

		mockMvc.perform(get("/api/admin/categories")
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size").value(10))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.totalItems").value(13))
				.andExpect(jsonPath("$.totalPages").value(2))
				.andExpect(jsonPath("$.items.length()").value(10));

		mockMvc.perform(get("/api/admin/categories")
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.param("page", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(3))
				.andExpect(jsonPath("$.page").value(1))
				.andExpect(jsonPath("$.hasPrevious").value(true));

		mockMvc.perform(get("/api/admin/categories")
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.param("search", "บริการ"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalItems").value(1))
				.andExpect(jsonPath("$.items[0].name").value("บริการทั่วไป"));
	}

	@Test
	void detailUpdateAndDuplicateValidation_work() throws Exception {
		Integer firstCategoryId = createCategory("Plumbing");
		createCategory("Cleaning");

		mockMvc.perform(get("/api/admin/categories/{categoryId}", firstCategoryId)
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.categoryId").value(firstCategoryId))
				.andExpect(jsonPath("$.name").value("Plumbing"));

		mockMvc.perform(put("/api/admin/categories/{categoryId}", firstCategoryId)
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Electrical"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Electrical"));

		mockMvc.perform(put("/api/admin/categories/{categoryId}", firstCategoryId)
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Cleaning"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Category name already exists"));
	}

	@Test
	void deleteBlockedWhenCategoryInUse_returns409() throws Exception {
		Integer categoryId = createCategory("Painting");
		serviceItemRepository.save(new ServiceItem(1, categoryId));

		mockMvc.perform(delete("/api/admin/categories/{categoryId}", categoryId)
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Category cannot be deleted because it is in use"));
	}

	@Test
	void deleteUnusedCategory_returns204() throws Exception {
		Integer categoryId = createCategory("Gardening");

		mockMvc.perform(delete("/api/admin/categories/{categoryId}", categoryId)
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isNoContent());
	}

	@Test
	void reorderPageScope_updatesVisibleOrder() throws Exception {
		List<Integer> ids = createCategories("Category", 12);
		List<Integer> pageZeroIds = ids.subList(0, 10);
		List<Integer> reordered = new ArrayList<>(pageZeroIds);
		Integer first = reordered.remove(0);
		reordered.add(first);

		mockMvc.perform(put("/api/admin/categories/reorder")
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ReorderPayload("page", reordered, null, 0))))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/admin/categories")
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].categoryId").value(reordered.get(0)))
				.andExpect(jsonPath("$.items[9].categoryId").value(reordered.get(9)));
	}

	@Test
	void reorderFilteredScope_updatesOnlyFilteredSubset() throws Exception {
		Integer a = createCategory("บริการทั่วไป");
		Integer b = createCategory("บริการห้องครัว");
		Integer c = createCategory("บริการห้องน้ำ");
		Integer d = createCategory("Electrical");

		mockMvc.perform(put("/api/admin/categories/reorder")
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ReorderPayload("filtered", List.of(c, a, b), "บริการ", null))))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/admin/categories")
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].categoryId").value(c))
				.andExpect(jsonPath("$.items[1].categoryId").value(a))
				.andExpect(jsonPath("$.items[2].categoryId").value(b))
				.andExpect(jsonPath("$.items[3].categoryId").value(d));
	}

	@Test
	void reorderPayloadMismatch_returns400() throws Exception {
		createCategories("Category", 3);

		mockMvc.perform(put("/api/admin/categories/reorder")
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"scope":"page","page":0,"categoryIds":[1,2]}
								"""))
				.andExpect(status().isBadRequest());
	}

	private Integer createCategory(String name) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/admin/categories")
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new NamePayload(name))))
				.andExpect(status().isCreated())
				.andReturn();

		JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
		return node.get("categoryId").asInt();
	}

	private List<Integer> createCategories(String prefix, int count) throws Exception {
		List<Integer> ids = new ArrayList<>();
		for (int i = 1; i <= count; i++) {
			ids.add(createCategory(prefix + " " + i));
		}
		return ids;
	}

	private String registerAdminAndGetToken() throws Exception {
		var payload = new AdminRegisterRequest(
				"Admin",
				"0999999999",
				"admin-category-" + System.nanoTime() + "@example.com",
				"password123",
				"test-invite"
		);

		String response = mockMvc.perform(post("/api/admin/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(payload)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		return objectMapper.readTree(response).get("accessToken").asText();
	}

	private String bearer() {
		return "Bearer " + adminToken;
	}

	private record NamePayload(String name) {
	}

	private record ReorderPayload(String scope, List<Integer> categoryIds, String search, Integer page) {
	}
}
