package com.homeservice.homeservice_server.controllers.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeservice.homeservice_server.dto.supabase.SupabaseGetUserResponse;
import com.homeservice.homeservice_server.entities.Category;
import com.homeservice.homeservice_server.entities.User;
import com.homeservice.homeservice_server.enums.UserRole;
import com.homeservice.homeservice_server.repositories.admin.AdminCategoryRepository;
import com.homeservice.homeservice_server.repositories.admin.AdminServiceRepository;
import com.homeservice.homeservice_server.repositories.admin.AdminSubServiceRepository;
import com.homeservice.homeservice_server.repositories.UserRepository;
import com.homeservice.homeservice_server.services.SupabaseAuthClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminServiceIntegrationTests {
	private MockMvc mockMvc;
	private String adminToken;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private AdminCategoryRepository adminCategoryRepository;

	@Autowired
	private AdminServiceRepository adminServiceRepository;

	@Autowired
	private AdminSubServiceRepository adminSubServiceRepository;

	@Autowired
	private UserRepository userRepository;

	@MockitoBean
	private SupabaseAuthClient supabaseAuthClient;

	@Autowired
	void setUp(WebApplicationContext context, FilterChainProxy springSecurityFilterChain) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.addFilters(springSecurityFilterChain)
				.build();
	}

	@BeforeEach
	void resetData() {
		adminSubServiceRepository.deleteAll();
		adminServiceRepository.deleteAll();
		adminCategoryRepository.deleteAll();
		userRepository.deleteAll();
		this.adminToken = createUserAndToken(UserRole.ADMIN, "admin-service");
	}

	@Test
	void services_withoutToken_returns401() throws Exception {
		mockMvc.perform(get("/api/admin/services"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void services_withNonAdminToken_returns403() throws Exception {
		String userToken = createUserAndToken(UserRole.CUSTOMER, "normal-user");

		mockMvc.perform(get("/api/admin/services")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isForbidden());
	}

	@Test
	void createListAndDetail_withPaginationSearchAndCategoryFilter_work() throws Exception {
		Integer homeCategoryId = createCategory("Home");
		Integer officeCategoryId = createCategory("Office");

		Integer firstServiceId = null;
		for (int i = 1; i <= 11; i++) {
			Integer createdId = createService(homeCategoryId, "Service " + i, "https://img/" + i + ".png");
			if (firstServiceId == null) {
				firstServiceId = createdId;
			}
		}
		createService(homeCategoryId, "Deep Cleaning", "https://img/deep.png");
		createService(officeCategoryId, "Office Setup", "https://img/office.png");

		mockMvc.perform(get("/api/admin/services")
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size").value(10))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.totalItems").value(13))
				.andExpect(jsonPath("$.totalPages").value(2))
				.andExpect(jsonPath("$.items.length()").value(10))
				.andExpect(jsonPath("$.items[0].subServices.length()").value(2));

		mockMvc.perform(get("/api/admin/services")
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.param("page", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(3))
				.andExpect(jsonPath("$.page").value(1));

		mockMvc.perform(get("/api/admin/services")
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.param("search", "Deep"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalItems").value(1))
				.andExpect(jsonPath("$.items[0].name").value("Deep Cleaning"));

		mockMvc.perform(get("/api/admin/services")
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.param("categoryId", officeCategoryId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalItems").value(1))
				.andExpect(jsonPath("$.items[0].name").value("Office Setup"));

		mockMvc.perform(get("/api/admin/services/{serviceId}", firstServiceId)
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.serviceId").value(firstServiceId))
				.andExpect(jsonPath("$.categoryId").value(homeCategoryId))
				.andExpect(jsonPath("$.subServices.length()").value(2))
				.andExpect(jsonPath("$.subServices[0].name").value("Package A"));
	}

	@Test
	void updateAndDuplicateValidation_work() throws Exception {
		Integer categoryId = createCategory("Cleaning");
		Integer secondCategoryId = createCategory("Repair");
		Integer firstServiceId = createService(categoryId, "Air Care", "https://img/air.png");
		createService(categoryId, "Window Care", "https://img/window.png");
		Integer existingSubServiceId = getSubServiceIds(firstServiceId).getFirst();

		mockMvc.perform(put("/api/admin/services/{serviceId}", firstServiceId)
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ServiceUpdatePayload(
								secondCategoryId,
								"Air Repair",
								"https://img/repair.png",
								List.of(
										new SubServiceUpdatePayload(existingSubServiceId, "Repair Basic", "job", new BigDecimal("800")),
										new SubServiceUpdatePayload(null, "Repair Plus", "job", new BigDecimal("1200"))
								)
						))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.categoryId").value(secondCategoryId))
				.andExpect(jsonPath("$.name").value("Air Repair"))
				.andExpect(jsonPath("$.imageUrl").value("https://img/repair.png"))
				.andExpect(jsonPath("$.subServices.length()").value(2))
				.andExpect(jsonPath("$.subServices[0].subServiceId").value(existingSubServiceId))
				.andExpect(jsonPath("$.subServices[0].name").value("Repair Basic"));

		mockMvc.perform(put("/api/admin/services/{serviceId}", firstServiceId)
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ServiceUpdatePayload(
								categoryId,
								"Window Care",
								"https://img/duplicate.png",
								List.of(new SubServiceUpdatePayload(existingSubServiceId, "Duplicate", "job", new BigDecimal("500")))
						))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Service name already exists in this category"));
	}

	@Test
	void updateSubServices_preservesExistingId_addsNewAndDeletesMissing() throws Exception {
		Integer categoryId = createCategory("Preserve");
		Integer serviceId = createService(categoryId, "Air Care", "https://img/air.png");
		List<Integer> subServiceIds = getSubServiceIds(serviceId);
		Integer keepId = subServiceIds.get(0);
		Integer removeId = subServiceIds.get(1);

		mockMvc.perform(put("/api/admin/services/{serviceId}", serviceId)
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ServiceUpdatePayload(
								categoryId,
								"Air Care Updated",
								"https://img/air-updated.png",
								List.of(
										new SubServiceUpdatePayload(keepId, "Package A Updated", "unit", new BigDecimal("150")),
										new SubServiceUpdatePayload(null, "Package C", "unit", new BigDecimal("300"))
								)
						))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.subServices.length()").value(2))
				.andExpect(jsonPath("$.subServices[0].subServiceId").value(keepId))
				.andExpect(jsonPath("$.subServices[0].name").value("Package A Updated"));

		List<Integer> updatedIds = getSubServiceIds(serviceId);
		org.junit.jupiter.api.Assertions.assertTrue(updatedIds.contains(keepId));
		org.junit.jupiter.api.Assertions.assertFalse(updatedIds.contains(removeId));
		org.junit.jupiter.api.Assertions.assertEquals(2, updatedIds.size());
	}

	@Test
	void updateWithForeignOrDuplicateSubServiceId_returns400() throws Exception {
		Integer categoryId = createCategory("Validation");
		Integer serviceA = createService(categoryId, "Service A", null);
		Integer serviceB = createService(categoryId, "Service B", null);
		Integer foreignSubServiceId = getSubServiceIds(serviceB).getFirst();
		Integer ownSubServiceId = getSubServiceIds(serviceA).getFirst();

		mockMvc.perform(put("/api/admin/services/{serviceId}", serviceA)
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ServiceUpdatePayload(
								categoryId,
								"Service A Updated",
								null,
								List.of(new SubServiceUpdatePayload(foreignSubServiceId, "Foreign", "unit", new BigDecimal("100")))
						))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Sub-service does not belong to this service"));

		mockMvc.perform(put("/api/admin/services/{serviceId}", serviceA)
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ServiceUpdatePayload(
								categoryId,
								"Service A Updated",
								null,
								List.of(
										new SubServiceUpdatePayload(ownSubServiceId, "One", "unit", new BigDecimal("100")),
										new SubServiceUpdatePayload(ownSubServiceId, "Two", "unit", new BigDecimal("200"))
								)
						))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("subServiceIds contains duplicates"));
	}

	@Test
	void patchService_updatesOnlyImageUrl() throws Exception {
		Integer categoryId = createCategory("Patch");
		Integer serviceId = createService(categoryId, "Patch Service", "https://img/original.png");

		mockMvc.perform(patch("/api/admin/services/{serviceId}", serviceId)
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"imageUrl":"https://img/updated.png"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Patch Service"))
				.andExpect(jsonPath("$.imageUrl").value("https://img/updated.png"))
				.andExpect(jsonPath("$.subServices.length()").value(2));
	}

	@Test
	void patchService_updatesOnlySubServiceFields_andPreservesId() throws Exception {
		Integer categoryId = createCategory("Patch SubService");
		Integer serviceId = createService(categoryId, "Patch SubService", null);
		Integer subServiceId = getSubServiceIds(serviceId).getFirst();

		mockMvc.perform(patch("/api/admin/services/{serviceId}", serviceId)
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ServicePatchPayload(
								null,
								null,
								null,
								List.of(new SubServicePatchPayload(subServiceId, null, "4 เครื่อง", new BigDecimal("100000")))
						))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.subServices[0].subServiceId").value(subServiceId))
				.andExpect(jsonPath("$.subServices[0].unit").value("4 เครื่อง"))
				.andExpect(jsonPath("$.subServices[0].pricePerUnit").value(100000))
				.andExpect(jsonPath("$.subServices[0].name").value("Package A"));
	}

	@Test
	void patchService_createsNewSubService_whenIdMissing() throws Exception {
		Integer categoryId = createCategory("Patch Create");
		Integer serviceId = createService(categoryId, "Patch Create", null);
		List<Integer> beforeIds = getSubServiceIds(serviceId);

		mockMvc.perform(patch("/api/admin/services/{serviceId}", serviceId)
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ServicePatchPayload(
								null,
								null,
								null,
								List.of(new SubServicePatchPayload(null, "Package C", "unit", new BigDecimal("300")))
						))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.subServices.length()").value(3))
				.andExpect(jsonPath("$.subServices[2].name").value("Package C"));

		List<Integer> afterIds = getSubServiceIds(serviceId);
		org.junit.jupiter.api.Assertions.assertEquals(3, adminSubServiceRepository.count());
		org.junit.jupiter.api.Assertions.assertTrue(afterIds.containsAll(beforeIds));
	}

	@Test
	void patchService_withInvalidSubServiceIdOrEmptyPayload_returns400() throws Exception {
		Integer categoryId = createCategory("Patch Validation");
		Integer serviceId = createService(categoryId, "Patch Validation", null);
		Integer subServiceId = getSubServiceIds(serviceId).getFirst();

		mockMvc.perform(patch("/api/admin/services/{serviceId}", serviceId)
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("At least one field is required for patch"));

		mockMvc.perform(patch("/api/admin/services/{serviceId}", serviceId)
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"subServices":[{"subServiceId":999999,"unit":"2 เครื่อง"}]}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Sub-service does not belong to this service"));

		mockMvc.perform(patch("/api/admin/services/{serviceId}", serviceId)
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ServicePatchPayload(
								null,
								null,
								null,
								List.of(new SubServicePatchPayload(subServiceId, null, null, null))
						))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Sub-service patch must include at least one mutable field"));
	}

	@Test
	void create_withUnknownCategoryAndInvalidSubServicePrice_fails() throws Exception {
		mockMvc.perform(post("/api/admin/services")
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ServicePayload(
								99999,
								"Unknown Category Service",
								null,
								List.of(new SubServicePayload("Basic", "job", new BigDecimal("100")))
						))))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Category not found"));

		Integer categoryId = createCategory("Validation");
		mockMvc.perform(post("/api/admin/services")
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ServicePayload(
								categoryId,
								"Invalid Price",
								null,
								List.of(new SubServicePayload("Basic", "job", new BigDecimal("-1")))
						))))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deleteService_withoutSubServices_returns204() throws Exception {
		Integer categoryId = createCategory("Delete");
		Integer serviceId = createService(categoryId, "Temporary Service", "https://img/delete.png");
		for (Integer subServiceId : getSubServiceIds(serviceId)) {
			adminSubServiceRepository.deleteById(subServiceId);
		}

		mockMvc.perform(delete("/api/admin/services/{serviceId}", serviceId)
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isNoContent());

		org.junit.jupiter.api.Assertions.assertEquals(0L, adminServiceRepository.count());
		org.junit.jupiter.api.Assertions.assertEquals(0L, adminSubServiceRepository.count());
	}

	@Test
	void deleteService_withSubServices_requiresForceDelete() throws Exception {
		Integer categoryId = createCategory("Delete Force");
		Integer serviceId = createService(categoryId, "Temporary Service", "https://img/delete.png");

		mockMvc.perform(delete("/api/admin/services/{serviceId}", serviceId)
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Service cannot be deleted because it has sub-services"));

		org.junit.jupiter.api.Assertions.assertEquals(1L, adminServiceRepository.count());
		org.junit.jupiter.api.Assertions.assertEquals(2L, adminSubServiceRepository.count());

		mockMvc.perform(delete("/api/admin/services/{serviceId}", serviceId)
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.param("force", "true"))
				.andExpect(status().isNoContent());

		org.junit.jupiter.api.Assertions.assertEquals(0L, adminServiceRepository.count());
		org.junit.jupiter.api.Assertions.assertEquals(0L, adminSubServiceRepository.count());
	}

	@Test
	void deleteSubService_returns204AndRemovesOnlyRequestedSubService() throws Exception {
		Integer categoryId = createCategory("Delete SubService");
		Integer serviceId = createService(categoryId, "Service With SubServices", null);
		List<Integer> subServiceIds = getSubServiceIds(serviceId);
		Integer removedId = subServiceIds.getFirst();
		Integer keptId = subServiceIds.get(1);

		mockMvc.perform(delete("/api/admin/services/{serviceId}/sub-services/{subServiceId}", serviceId, removedId)
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isNoContent());

		List<Integer> remainingIds = getSubServiceIds(serviceId);
		org.junit.jupiter.api.Assertions.assertFalse(remainingIds.contains(removedId));
		org.junit.jupiter.api.Assertions.assertTrue(remainingIds.contains(keptId));
		org.junit.jupiter.api.Assertions.assertEquals(1, remainingIds.size());
	}

	@Test
	void deleteSubService_withForeignOrUnknownId_returnsError() throws Exception {
		Integer categoryId = createCategory("Delete SubService Validation");
		Integer firstServiceId = createService(categoryId, "Service One", null);
		Integer secondServiceId = createService(categoryId, "Service Two", null);
		Integer foreignSubServiceId = getSubServiceIds(secondServiceId).getFirst();

		mockMvc.perform(delete("/api/admin/services/{serviceId}/sub-services/{subServiceId}", firstServiceId, foreignSubServiceId)
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Sub-service does not belong to this service"));

		mockMvc.perform(delete("/api/admin/services/{serviceId}/sub-services/{subServiceId}", firstServiceId, 999999)
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Sub-service not found"));
	}

	@Test
	void reorderPageScope_updatesVisibleOrder() throws Exception {
		Integer categoryId = createCategory("Page Reorder");
		List<Integer> ids = new ArrayList<>();
		for (int i = 1; i <= 12; i++) {
			ids.add(createService(categoryId, "Page Service " + i, null));
		}

		List<Integer> pageZeroIds = new ArrayList<>(ids.subList(0, 10));
		Integer first = pageZeroIds.remove(0);
		pageZeroIds.add(first);

		mockMvc.perform(put("/api/admin/services/reorder")
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ReorderPayload("page", pageZeroIds, null, categoryId, 0))))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/admin/services")
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].serviceId").value(pageZeroIds.get(0)))
				.andExpect(jsonPath("$.items[9].serviceId").value(pageZeroIds.get(9)));
	}

	@Test
	void reorderFilteredScope_updatesOnlyMatchingSubset() throws Exception {
		Integer cleaningCategoryId = createCategory("Cleaning");
		Integer repairCategoryId = createCategory("Repair");
		Integer a = createService(cleaningCategoryId, "Cleaning Basic", null);
		Integer b = createService(cleaningCategoryId, "Cleaning Plus", null);
		Integer c = createService(repairCategoryId, "Repair Basic", null);

		mockMvc.perform(put("/api/admin/services/reorder")
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ReorderPayload(
								"filtered",
								List.of(b, a),
								null,
								cleaningCategoryId,
								null
						))))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/admin/services")
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].serviceId").value(b))
				.andExpect(jsonPath("$.items[1].serviceId").value(a))
				.andExpect(jsonPath("$.items[2].serviceId").value(c));
	}

	private Integer createCategory(String name) {
		int nextSortOrder = (int) adminCategoryRepository.count() + 1;
		Category category = Category.builder()
				.name(name)
				.sortOrder(nextSortOrder)
				.build();
		return adminCategoryRepository.save(category).getCategoryId();
	}

	private Integer createService(Integer categoryId, String name, String imageUrl) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/admin/services")
						.header(HttpHeaders.AUTHORIZATION, bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ServicePayload(
								categoryId,
								name,
								imageUrl,
								List.of(
										new SubServicePayload("Package A", "unit", new BigDecimal("100")),
										new SubServicePayload("Package B", "unit", new BigDecimal("200"))
								)
						))))
				.andExpect(status().isCreated())
				.andReturn();

		JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
		return node.get("serviceId").asInt();
	}

	private List<Integer> getSubServiceIds(Integer serviceId) throws Exception {
		MvcResult result = mockMvc.perform(get("/api/admin/services/{serviceId}", serviceId)
						.header(HttpHeaders.AUTHORIZATION, bearer()))
				.andExpect(status().isOk())
				.andReturn();

		JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString()).get("subServices");
		List<Integer> ids = new ArrayList<>();
		for (JsonNode item : node) {
			ids.add(item.get("subServiceId").asInt());
		}
		return ids;
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

	private record ServicePayload(
			Integer categoryId,
			String name,
			String imageUrl,
			List<SubServicePayload> subServices
	) {
	}

	private record SubServicePayload(
			String name,
			String unit,
			BigDecimal pricePerUnit
	) {
	}

	private record ServiceUpdatePayload(
			Integer categoryId,
			String name,
			String imageUrl,
			List<SubServiceUpdatePayload> subServices
	) {
	}

	private record SubServiceUpdatePayload(
			Integer subServiceId,
			String name,
			String unit,
			BigDecimal pricePerUnit
	) {
	}

	private record ReorderPayload(
			String scope,
			List<Integer> serviceIds,
			String search,
			Integer categoryId,
			Integer page
	) {
	}

	private record ServicePatchPayload(
			Integer categoryId,
			String name,
			String imageUrl,
			List<SubServicePatchPayload> subServices
	) {
	}

	private record SubServicePatchPayload(
			Integer subServiceId,
			String name,
			String unit,
			BigDecimal pricePerUnit
	) {
	}
}
