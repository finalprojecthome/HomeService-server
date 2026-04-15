package com.homeservice.homeservice_server.services.admin;

import com.homeservice.homeservice_server.entities.Category;
import com.homeservice.homeservice_server.entities.ServiceItem;
import com.homeservice.homeservice_server.entities.SubServiceItem;
import com.homeservice.homeservice_server.exception.ConflictException;
import com.homeservice.homeservice_server.exception.NotFoundException;
import com.homeservice.homeservice_server.exception.ValidationException;
import com.homeservice.homeservice_server.dto.admin.service.AdminServiceCreateRequest;
import com.homeservice.homeservice_server.dto.admin.service.AdminServiceDeleteImpactResponse;
import com.homeservice.homeservice_server.dto.admin.service.AdminServicePageResponse;
import com.homeservice.homeservice_server.dto.admin.service.AdminServicePatchRequest;
import com.homeservice.homeservice_server.dto.admin.service.AdminServiceResponse;
import com.homeservice.homeservice_server.dto.admin.service.AdminServiceUpdateRequest;
import com.homeservice.homeservice_server.dto.admin.service.AdminSubServicePatchRequest;
import com.homeservice.homeservice_server.dto.admin.service.AdminSubServiceRequest;
import com.homeservice.homeservice_server.dto.admin.service.AdminSubServiceResponse;
import com.homeservice.homeservice_server.dto.admin.service.AdminSubServiceUpdateRequest;
import com.homeservice.homeservice_server.repositories.admin.AdminCategoryRepository;
import com.homeservice.homeservice_server.repositories.admin.AdminServiceRepository;
import com.homeservice.homeservice_server.repositories.admin.AdminSubServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminServiceService {
	private static final int PAGE_SIZE = 10;
	private static final Sort SERVICE_SORT =
			Sort.by(Sort.Order.asc("sortOrder"), Sort.Order.asc("serviceId"));

	private final AdminServiceRepository adminServiceRepository;
	private final AdminSubServiceRepository adminSubServiceRepository;
	private final AdminCategoryRepository adminCategoryRepository;

	public AdminServiceService(
			AdminServiceRepository adminServiceRepository,
			AdminSubServiceRepository adminSubServiceRepository,
			AdminCategoryRepository adminCategoryRepository
	) {
		this.adminServiceRepository = adminServiceRepository;
		this.adminSubServiceRepository = adminSubServiceRepository;
		this.adminCategoryRepository = adminCategoryRepository;
	}

	@Transactional
	public AdminServiceResponse createService(AdminServiceCreateRequest request) {
		initializeMissingSortOrders();
		Integer categoryId = requireCategory(request.categoryId()).getCategoryId();
		String name = normalizeRequiredText(request.name(), "Service name is required");
		String imageUrl = normalizeOptionalText(request.imageUrl());
		validateDuplicateName(categoryId, name, null);

		int nextSortOrder = adminServiceRepository.findTopByOrderBySortOrderDescServiceIdDesc()
				.map(service -> service.getSortOrder() + 1)
				.orElse(1);

		ServiceItem service = ServiceItem.builder()
				.categoryId(categoryId)
				.name(name)
				.imageUrl(imageUrl)
				.sortOrder(nextSortOrder)
				.build();

		ServiceItem saved = adminServiceRepository.save(service);
		backfillSortOrderIfNeeded(saved);
		List<SubServiceItem> subServices = replaceSubServices(saved.getServiceId(), request.subServices());
		return toResponse(saved, subServices);
	}

	@Transactional(readOnly = true)
	public AdminServicePageResponse getServices(String search, Integer categoryId, Integer page) {
		initializeMissingSortOrders();
		Page<ServiceItem> servicePage = findServicePage(search, categoryId, page);
		List<ServiceItem> items = servicePage.getContent();
		Map<Integer, List<SubServiceItem>> subServicesByServiceId = getSubServicesByServiceIds(items);

		List<AdminServiceResponse> responses = items.stream()
				.map(service -> toResponse(service, subServicesByServiceId.getOrDefault(service.getServiceId(), List.of())))
				.toList();

		return new AdminServicePageResponse(
				responses,
				servicePage.getNumber(),
				servicePage.getSize(),
				servicePage.getTotalElements(),
				servicePage.getTotalPages(),
				servicePage.hasNext(),
				servicePage.hasPrevious()
		);
	}

	@Transactional(readOnly = true)
	public AdminServiceResponse getServiceById(Integer serviceId) {
		ServiceItem service = requireService(serviceId);
		List<SubServiceItem> subServices = adminSubServiceRepository.findByServiceIdOrderBySubServiceIdAsc(serviceId);
		return toResponse(service, subServices);
	}

	@Transactional(readOnly = true)
	public AdminServiceDeleteImpactResponse getDeleteImpact(Integer serviceId) {
		ServiceItem service = requireService(serviceId);
		boolean requiresForceDelete = adminSubServiceRepository.existsByServiceId(serviceId);
		return new AdminServiceDeleteImpactResponse(service.getServiceId(), requiresForceDelete);
	}

	@Transactional
	public AdminServiceResponse updateService(Integer serviceId, AdminServiceUpdateRequest request) {
		ServiceItem service = requireService(serviceId);
		Integer categoryId = requireCategory(request.categoryId()).getCategoryId();
		String name = normalizeRequiredText(request.name(), "Service name is required");
		String imageUrl = normalizeOptionalText(request.imageUrl());
		validateDuplicateName(categoryId, name, serviceId);

		service.setCategoryId(categoryId);
		service.setName(name);
		service.setImageUrl(imageUrl);

		ServiceItem saved = adminServiceRepository.save(service);
		List<SubServiceItem> subServices = syncSubServicesForUpdate(saved.getServiceId(), request.subServices());
		return toResponse(saved, subServices);
	}

	@Transactional
	public AdminServiceResponse patchService(Integer serviceId, AdminServicePatchRequest request) {
		ServiceItem service = requireService(serviceId);
		validatePatchRequest(request);

		Integer categoryId = service.getCategoryId();
		if (request.categoryId() != null) {
			categoryId = requireCategory(request.categoryId()).getCategoryId();
		}

		String name = service.getName();
		if (request.name() != null) {
			name = normalizeRequiredText(request.name(), "Service name is required");
		}

		String imageUrl = service.getImageUrl();
		if (request.imageUrl() != null) {
			imageUrl = normalizeOptionalText(request.imageUrl());
		}

		validateDuplicateName(categoryId, name, serviceId);

		service.setCategoryId(categoryId);
		service.setName(name);
		service.setImageUrl(imageUrl);

		ServiceItem saved = adminServiceRepository.save(service);
		List<SubServiceItem> subServices = request.subServices() == null
				? adminSubServiceRepository.findByServiceIdOrderBySubServiceIdAsc(saved.getServiceId())
				: patchSubServices(saved.getServiceId(), request.subServices());

		return toResponse(saved, subServices);
	}

	@Transactional
	public void deleteService(Integer serviceId, boolean force) {
		ServiceItem service = requireService(serviceId);
		boolean hasSubServices = adminSubServiceRepository.existsByServiceId(serviceId);
		if (hasSubServices && !force) {
			throw new ConflictException("Service cannot be deleted because it has sub-services");
		}

		Integer deletedSortOrder = service.getSortOrder();

		if (hasSubServices) {
			adminSubServiceRepository.deleteAllByServiceId(serviceId);
		}

		adminServiceRepository.delete(service);
		shiftSortOrdersAfterDelete(deletedSortOrder);
	}

	@Transactional
	public void deleteSubService(Integer serviceId, Integer subServiceId) {
		requireService(serviceId);
		SubServiceItem subService = requireSubServiceForService(serviceId, subServiceId);
		adminSubServiceRepository.delete(subService);
	}

	@Transactional
	public void reorderServices(
			String scope,
			List<Integer> orderedServiceIds,
			String search,
			Integer categoryId,
			Integer page
	) {
		initializeMissingSortOrders();
		String normalizedScope = normalizeScope(scope);
		List<ServiceItem> allServices = adminServiceRepository.findAllByOrderBySortOrderAscServiceIdAsc();

		if (allServices.isEmpty()) {
			throw new ValidationException("No services to reorder");
		}

		List<Integer> allIds = allServices.stream()
				.map(ServiceItem::getServiceId)
				.toList();
		List<Integer> targetIds = getTargetServiceIds(normalizedScope, allIds, search, categoryId, page);

		validateReorderPayload(targetIds, orderedServiceIds);

		Set<Integer> targetIdSet = new HashSet<>(targetIds);
		List<Integer> mergedOrder = new ArrayList<>(allIds.size());
		int replacementIndex = 0;

		for (Integer currentServiceId : allIds) {
			if (targetIdSet.contains(currentServiceId)) {
				mergedOrder.add(orderedServiceIds.get(replacementIndex++));
			} else {
				mergedOrder.add(currentServiceId);
			}
		}

		Map<Integer, ServiceItem> serviceMap = new HashMap<>();
		for (ServiceItem service : allServices) {
			serviceMap.put(service.getServiceId(), service);
		}

		for (int index = 0; index < mergedOrder.size(); index++) {
			ServiceItem service = serviceMap.get(mergedOrder.get(index));
			service.setSortOrder(index + 1);
		}

		adminServiceRepository.saveAll(allServices);
	}

	private Category requireCategory(Integer categoryId) {
		return adminCategoryRepository.findById(categoryId)
				.orElseThrow(() -> new NotFoundException("Category not found"));
	}

	private ServiceItem requireService(Integer serviceId) {
		return adminServiceRepository.findById(serviceId)
				.orElseThrow(() -> new NotFoundException("Service not found"));
	}

	private SubServiceItem requireSubServiceForService(Integer serviceId, Integer subServiceId) {
		SubServiceItem subService = adminSubServiceRepository.findById(subServiceId)
				.orElseThrow(() -> new NotFoundException("Sub-service not found"));

		if (!serviceId.equals(subService.getServiceId())) {
			throw new ValidationException("Sub-service does not belong to this service");
		}

		return subService;
	}

	private void validateDuplicateName(Integer categoryId, String name, Integer serviceId) {
		boolean exists = serviceId == null
				? adminServiceRepository.existsByCategoryIdAndNameIgnoreCase(categoryId, name)
				: adminServiceRepository.existsByCategoryIdAndNameIgnoreCaseAndServiceIdNot(categoryId, name, serviceId);

		if (exists) {
			throw new ValidationException("Service name already exists in this category");
		}
	}

	private List<SubServiceItem> replaceSubServices(Integer serviceId, List<AdminSubServiceRequest> requests) {
		if (requests == null || requests.isEmpty()) {
			throw new ValidationException("subServices is required");
		}

		adminSubServiceRepository.deleteAllByServiceId(serviceId);

		List<SubServiceItem> subServices = requests.stream()
				.map(request -> SubServiceItem.builder()
						.serviceId(serviceId)
						.name(normalizeRequiredText(request.name(), "Sub-service name is required"))
						.unit(normalizeRequiredText(request.unit(), "Sub-service unit is required"))
						.pricePerUnit(requireNonNegativePrice(request.pricePerUnit()))
						.build())
				.toList();

		return adminSubServiceRepository.saveAll(subServices);
	}

	private List<SubServiceItem> syncSubServicesForUpdate(
			Integer serviceId,
			List<AdminSubServiceUpdateRequest> requests
	) {
		if (requests == null || requests.isEmpty()) {
			throw new ValidationException("subServices is required");
		}

		List<SubServiceItem> existingSubServices = adminSubServiceRepository.findByServiceIdOrderBySubServiceIdAsc(serviceId);
		Map<Integer, SubServiceItem> existingById = existingSubServices.stream()
				.collect(Collectors.toMap(SubServiceItem::getSubServiceId, subService -> subService));

		Set<Integer> referencedIds = new LinkedHashSet<>();
		List<SubServiceItem> result = new ArrayList<>();

		for (AdminSubServiceUpdateRequest request : requests) {
			Integer subServiceId = request.subServiceId();
			if (subServiceId != null) {
				if (!referencedIds.add(subServiceId)) {
					throw new ValidationException("subServiceIds contains duplicates");
				}

				SubServiceItem existing = existingById.get(subServiceId);
				if (existing == null) {
					throw new ValidationException("Sub-service does not belong to this service");
				}

				existing.setName(normalizeRequiredText(request.name(), "Sub-service name is required"));
				existing.setUnit(normalizeRequiredText(request.unit(), "Sub-service unit is required"));
				existing.setPricePerUnit(requireNonNegativePrice(request.pricePerUnit()));
				result.add(existing);
				continue;
			}

			SubServiceItem created = SubServiceItem.builder()
					.serviceId(serviceId)
					.name(normalizeRequiredText(request.name(), "Sub-service name is required"))
					.unit(normalizeRequiredText(request.unit(), "Sub-service unit is required"))
					.pricePerUnit(requireNonNegativePrice(request.pricePerUnit()))
					.build();
			result.add(created);
		}

		List<SubServiceItem> toDelete = existingSubServices.stream()
				.filter(subService -> !referencedIds.contains(subService.getSubServiceId()))
				.toList();

		if (!toDelete.isEmpty()) {
			adminSubServiceRepository.deleteAll(toDelete);
		}

		return adminSubServiceRepository.saveAll(result);
	}

	private List<SubServiceItem> patchSubServices(
			Integer serviceId,
			List<AdminSubServicePatchRequest> requests
	) {
		if (requests.isEmpty()) {
			throw new ValidationException("subServices must not be empty");
		}

		List<SubServiceItem> existingSubServices = adminSubServiceRepository.findByServiceIdOrderBySubServiceIdAsc(serviceId);
		Map<Integer, SubServiceItem> existingById = existingSubServices.stream()
				.collect(Collectors.toMap(SubServiceItem::getSubServiceId, subService -> subService));

		Set<Integer> referencedIds = new LinkedHashSet<>();
		List<SubServiceItem> toSave = new ArrayList<>();

		for (AdminSubServicePatchRequest request : requests) {
			validateSubServicePatchRequest(request);

			Integer subServiceId = request.subServiceId();
			if (subServiceId != null) {
				if (!referencedIds.add(subServiceId)) {
					throw new ValidationException("subServiceIds contains duplicates");
				}

				SubServiceItem existing = existingById.get(subServiceId);
				if (existing == null) {
					throw new ValidationException("Sub-service does not belong to this service");
				}

				if (request.name() != null) {
					existing.setName(normalizeRequiredText(request.name(), "Sub-service name is required"));
				}
				if (request.unit() != null) {
					existing.setUnit(normalizeRequiredText(request.unit(), "Sub-service unit is required"));
				}
				if (request.pricePerUnit() != null) {
					existing.setPricePerUnit(requireNonNegativePrice(request.pricePerUnit()));
				}
				toSave.add(existing);
				continue;
			}

			SubServiceItem created = SubServiceItem.builder()
					.serviceId(serviceId)
					.name(normalizeRequiredText(request.name(), "Sub-service name is required"))
					.unit(normalizeRequiredText(request.unit(), "Sub-service unit is required"))
					.pricePerUnit(requireNonNegativePrice(request.pricePerUnit()))
					.build();
			toSave.add(created);
		}

		adminSubServiceRepository.saveAll(toSave);
		return adminSubServiceRepository.findByServiceIdOrderBySubServiceIdAsc(serviceId);
	}

	private void validatePatchRequest(AdminServicePatchRequest request) {
		boolean hasTopLevelUpdate =
				request.categoryId() != null || request.name() != null || request.imageUrl() != null;
		boolean hasSubServiceUpdate = request.subServices() != null;

		if (!hasTopLevelUpdate && !hasSubServiceUpdate) {
			throw new ValidationException("At least one field is required for patch");
		}
	}

	private void validateSubServicePatchRequest(AdminSubServicePatchRequest request) {
		boolean hasMutableField =
				request.name() != null || request.unit() != null || request.pricePerUnit() != null;

		if (!hasMutableField) {
			throw new ValidationException("Sub-service patch must include at least one mutable field");
		}

		if (request.subServiceId() == null) {
			if (request.name() == null) {
				throw new ValidationException("Sub-service name is required for new sub-service");
			}
			if (request.unit() == null) {
				throw new ValidationException("Sub-service unit is required for new sub-service");
			}
			if (request.pricePerUnit() == null) {
				throw new ValidationException("Sub-service price is required for new sub-service");
			}
		}
	}

	private java.math.BigDecimal requireNonNegativePrice(java.math.BigDecimal pricePerUnit) {
		if (pricePerUnit == null) {
			throw new ValidationException("Sub-service price is required");
		}
		if (pricePerUnit.signum() < 0) {
			throw new ValidationException("Sub-service price must be greater than or equal to zero");
		}
		return pricePerUnit;
	}

	private String normalizeRequiredText(String rawValue, String message) {
		if (rawValue == null) {
			throw new ValidationException(message);
		}

		String normalized = rawValue.trim();
		if (normalized.isEmpty()) {
			throw new ValidationException(message);
		}
		return normalized;
	}

	private String normalizeOptionalText(String rawValue) {
		if (rawValue == null) {
			return null;
		}
		String normalized = rawValue.trim();
		return normalized.isEmpty() ? null : normalized;
	}

	private String normalizeScope(String scope) {
		if (scope == null || scope.isBlank()) {
			return "all";
		}
		return scope.trim().toLowerCase(Locale.ROOT);
	}

	private List<Integer> getTargetServiceIds(
			String scope,
			List<Integer> allIds,
			String search,
			Integer categoryId,
			Integer page
	) {
		return switch (scope) {
			case "all" -> allIds;
			case "filtered" -> getFilteredServiceIds(search, categoryId);
			case "page" -> getPagedServiceIds(search, categoryId, page);
			default -> throw new ValidationException("Invalid reorder scope");
		};
	}

	private List<Integer> getFilteredServiceIds(String search, Integer categoryId) {
		if (!hasText(search) && categoryId == null) {
			throw new ValidationException("Search or categoryId is required for filtered reorder");
		}

		return findServicesForScope(search, categoryId).stream()
				.map(ServiceItem::getServiceId)
				.toList();
	}

	private List<Integer> getPagedServiceIds(String search, Integer categoryId, Integer page) {
		Page<ServiceItem> servicePage = findServicePage(search, categoryId, page);
		List<Integer> ids = servicePage.getContent().stream()
				.map(ServiceItem::getServiceId)
				.toList();

		if (ids.isEmpty()) {
			throw new ValidationException("No services found for the requested page");
		}
		return ids;
	}

	private void validateReorderPayload(List<Integer> targetIds, List<Integer> orderedServiceIds) {
		if (orderedServiceIds == null || orderedServiceIds.isEmpty()) {
			throw new ValidationException("serviceIds is required");
		}

		if (targetIds.size() != orderedServiceIds.size()) {
			throw new ValidationException("serviceIds does not match the requested reorder scope");
		}

		Set<Integer> targetIdSet = new HashSet<>(targetIds);
		Set<Integer> orderedIdSet = new HashSet<>(orderedServiceIds);

		if (orderedIdSet.size() != orderedServiceIds.size()) {
			throw new ValidationException("serviceIds contains duplicates");
		}

		if (!targetIdSet.equals(orderedIdSet)) {
			throw new ValidationException("serviceIds does not match the requested reorder scope");
		}
	}

	private Page<ServiceItem> findServicePage(String search, Integer categoryId, Integer page) {
		Pageable pageable = PageRequest.of(normalizePage(page), PAGE_SIZE, SERVICE_SORT);
		if (categoryId != null && hasText(search)) {
			return adminServiceRepository.findByCategoryIdAndNameContainingIgnoreCaseOrderBySortOrderAscServiceIdAsc(
					categoryId,
					search.trim(),
					pageable
			);
		}
		if (categoryId != null) {
			return adminServiceRepository.findByCategoryIdOrderBySortOrderAscServiceIdAsc(categoryId, pageable);
		}
		if (hasText(search)) {
			return adminServiceRepository.findByNameContainingIgnoreCaseOrderBySortOrderAscServiceIdAsc(search.trim(), pageable);
		}
		return adminServiceRepository.findAllByOrderBySortOrderAscServiceIdAsc(pageable);
	}

	private List<ServiceItem> findServicesForScope(String search, Integer categoryId) {
		if (categoryId != null && hasText(search)) {
			return adminServiceRepository.findByCategoryIdAndNameContainingIgnoreCaseOrderBySortOrderAscServiceIdAsc(
					categoryId,
					search.trim()
			);
		}
		if (categoryId != null) {
			return adminServiceRepository.findByCategoryIdOrderBySortOrderAscServiceIdAsc(categoryId);
		}
		if (hasText(search)) {
			return adminServiceRepository.findByNameContainingIgnoreCaseOrderBySortOrderAscServiceIdAsc(search.trim());
		}
		return adminServiceRepository.findAllByOrderBySortOrderAscServiceIdAsc();
	}

	private int normalizePage(Integer page) {
		return page == null || page < 0 ? 0 : page;
	}

	private void normalizeSortOrders() {
		List<ServiceItem> services = adminServiceRepository.findAllByOrderBySortOrderAscServiceIdAsc();
		for (int index = 0; index < services.size(); index++) {
			services.get(index).setSortOrder(index + 1);
		}
		adminServiceRepository.saveAll(services);
	}

	private void shiftSortOrdersAfterDelete(Integer deletedSortOrder) {
		if (deletedSortOrder == null || deletedSortOrder <= 0) {
			normalizeSortOrders();
			return;
		}

		adminServiceRepository.decrementSortOrderGreaterThan(deletedSortOrder);
	}

	private void initializeMissingSortOrders() {
		List<ServiceItem> services = adminServiceRepository.findAll(SERVICE_SORT);
		boolean hasMissingSortOrder = services.stream()
				.anyMatch(service -> service.getSortOrder() == null || service.getSortOrder() <= 0);

		if (!hasMissingSortOrder) {
			return;
		}

		for (int index = 0; index < services.size(); index++) {
			services.get(index).setSortOrder(index + 1);
		}
		adminServiceRepository.saveAll(services);
	}

	private void backfillSortOrderIfNeeded(ServiceItem service) {
		if (service.getSortOrder() == null || service.getSortOrder() == 0) {
			service.setSortOrder(service.getServiceId());
			adminServiceRepository.save(service);
		}
	}

	private Map<Integer, List<SubServiceItem>> getSubServicesByServiceIds(List<ServiceItem> services) {
		if (services.isEmpty()) {
			return Map.of();
		}

		List<Integer> serviceIds = services.stream()
				.map(ServiceItem::getServiceId)
				.toList();

		return adminSubServiceRepository.findByServiceIdInOrderByServiceIdAscSubServiceIdAsc(serviceIds).stream()
				.collect(Collectors.groupingBy(SubServiceItem::getServiceId, HashMap::new, Collectors.toList()));
	}

	private AdminServiceResponse toResponse(ServiceItem service, List<SubServiceItem> subServices) {
		List<AdminSubServiceResponse> subServiceResponses = subServices.stream()
				.map(subService -> new AdminSubServiceResponse(
						subService.getSubServiceId(),
						subService.getName(),
						subService.getUnit(),
						subService.getPricePerUnit(),
						subService.getUpdatedAt()
				))
				.toList();

		return new AdminServiceResponse(
				service.getServiceId(),
				service.getCategoryId(),
				service.getName(),
				service.getImageUrl(),
				service.getSortOrder(),
				service.getCreatedAt(),
				service.getUpdatedAt(),
				subServiceResponses
		);
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
