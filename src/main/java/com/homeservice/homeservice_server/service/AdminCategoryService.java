package com.homeservice.homeservice_server.service;

import com.homeservice.homeservice_server.dto.AdminCategoryPageResponse;
import com.homeservice.homeservice_server.dto.AdminCategoryResponse;
import com.homeservice.homeservice_server.entity.Category;
import com.homeservice.homeservice_server.exception.ConflictException;
import com.homeservice.homeservice_server.exception.NotFoundException;
import com.homeservice.homeservice_server.exception.ValidationException;
import com.homeservice.homeservice_server.repository.AdminCategoryRepository;
import com.homeservice.homeservice_server.repository.ServiceItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class AdminCategoryService {
	private static final int PAGE_SIZE = 10;
	private static final Sort CATEGORY_SORT =
			Sort.by(Sort.Order.asc("sortOrder"), Sort.Order.asc("categoryId"));

	private final AdminCategoryRepository adminCategoryRepository;
	private final ServiceItemRepository serviceItemRepository;

	public AdminCategoryService(
			AdminCategoryRepository adminCategoryRepository,
			ServiceItemRepository serviceItemRepository
	) {
		this.adminCategoryRepository = adminCategoryRepository;
		this.serviceItemRepository = serviceItemRepository;
	}

	@Transactional
	public AdminCategoryResponse createCategory(String rawName) {
		String name = normalizeName(rawName);
		validateDuplicateName(name, null);

		int nextSortOrder = adminCategoryRepository.findTopByOrderBySortOrderDescCategoryIdDesc()
				.map(category -> category.getSortOrder() + 1)
				.orElse(1);

		Category category = Category.builder()
				.name(name)
				.sortOrder(nextSortOrder)
				.build();

		Category saved = adminCategoryRepository.save(category);
		backfillSortOrderIfNeeded(saved);
		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public AdminCategoryPageResponse getCategories(String search, Integer page) {
		Page<Category> categoryPage = findCategoryPage(search, page);

		List<AdminCategoryResponse> items = categoryPage.getContent().stream()
				.map(this::toResponse)
				.toList();

		return new AdminCategoryPageResponse(
				items,
				categoryPage.getNumber(),
				categoryPage.getSize(),
				categoryPage.getTotalElements(),
				categoryPage.getTotalPages(),
				categoryPage.hasNext(),
				categoryPage.hasPrevious()
		);
	}

	@Transactional(readOnly = true)
	public AdminCategoryResponse getCategoryById(Integer categoryId) {
		return toResponse(requireCategory(categoryId));
	}

	@Transactional
	public AdminCategoryResponse updateCategory(Integer categoryId, String rawName) {
		Category category = requireCategory(categoryId);
		String name = normalizeName(rawName);
		validateDuplicateName(name, categoryId);
		category.setName(name);
		return toResponse(adminCategoryRepository.save(category));
	}

	@Transactional
	public void deleteCategory(Integer categoryId, boolean force) {
		Category category = requireCategory(categoryId);
		boolean isInUse = serviceItemRepository.existsByCategoryId(categoryId);
		if (isInUse && !force) {
			throw new ConflictException("Category cannot be deleted because it is in use");
		}

		if (isInUse) {
			serviceItemRepository.deleteAllByCategoryId(categoryId);
		}

		adminCategoryRepository.delete(category);
		normalizeSortOrders();
	}

	@Transactional
	public void reorderCategories(String scope, List<Integer> orderedCategoryIds, String search, Integer page) {
		String normalizedScope = normalizeScope(scope);
		List<Category> allCategories = adminCategoryRepository.findAllByOrderBySortOrderAscCategoryIdAsc();

		if (allCategories.isEmpty()) {
			throw new ValidationException("No categories to reorder");
		}

		List<Integer> allIds = allCategories.stream()
				.map(Category::getCategoryId)
				.toList();
		List<Integer> targetIds = getTargetCategoryIds(normalizedScope, allIds, search, page);

		validateReorderPayload(targetIds, orderedCategoryIds);

		// Preserve the relative order of categories outside the requested reorder scope.
		Set<Integer> targetIdSet = new HashSet<>(targetIds);
		List<Integer> mergedOrder = new ArrayList<>(allIds.size());
		int replacementIndex = 0;

		for (Integer categoryId : allIds) {
			if (targetIdSet.contains(categoryId)) {
				mergedOrder.add(orderedCategoryIds.get(replacementIndex++));
			} else {
				mergedOrder.add(categoryId);
			}
		}

		Map<Integer, Category> categoryMap = new HashMap<>();
		for (Category category : allCategories) {
			categoryMap.put(category.getCategoryId(), category);
		}

		for (int index = 0; index < mergedOrder.size(); index++) {
			Category category = categoryMap.get(mergedOrder.get(index));
			category.setSortOrder(index + 1);
		}

		adminCategoryRepository.saveAll(allCategories);
	}

	private void validateDuplicateName(String name, Integer categoryId) {
		boolean exists = categoryId == null
				? adminCategoryRepository.existsByNameIgnoreCase(name)
				: adminCategoryRepository.existsByNameIgnoreCaseAndCategoryIdNot(name, categoryId);

		if (exists) {
			throw new ValidationException("Category name already exists");
		}
	}

	private Category requireCategory(Integer categoryId) {
		return adminCategoryRepository.findById(categoryId)
				.orElseThrow(() -> new NotFoundException("Category not found"));
	}

	private String normalizeName(String rawName) {
		if (rawName == null) {
			throw new ValidationException("Category name is required");
		}

		String name = rawName.trim();
		if (name.isEmpty()) {
			throw new ValidationException("Category name is required");
		}

		return name;
	}

	private String normalizeScope(String scope) {
		if (scope == null || scope.isBlank()) {
			return "all";
		}

		return scope.trim().toLowerCase(Locale.ROOT);
	}

	private List<Integer> getTargetCategoryIds(
			String scope,
			List<Integer> allIds,
			String search,
			Integer page
	) {
		return switch (scope) {
			case "all" -> allIds;
			case "filtered" -> getFilteredCategoryIds(search);
			case "page" -> getPagedCategoryIds(search, page);
			default -> throw new ValidationException("Invalid reorder scope");
		};
	}

	private List<Integer> getFilteredCategoryIds(String search) {
		if (!hasText(search)) {
			throw new ValidationException("Search is required for filtered reorder");
		}

		return adminCategoryRepository.findByNameContainingIgnoreCaseOrderBySortOrderAscCategoryIdAsc(search.trim()).stream()
				.map(Category::getCategoryId)
				.toList();
	}

	private List<Integer> getPagedCategoryIds(String search, Integer page) {
		Page<Category> categoryPage = findCategoryPage(search, page);

		List<Integer> ids = categoryPage.getContent().stream()
				.map(Category::getCategoryId)
				.toList();

		if (ids.isEmpty()) {
			throw new ValidationException("No categories found for the requested page");
		}

		return ids;
	}

	private void validateReorderPayload(List<Integer> targetIds, List<Integer> orderedCategoryIds) {
		if (orderedCategoryIds == null || orderedCategoryIds.isEmpty()) {
			throw new ValidationException("categoryIds is required");
		}

		if (targetIds.size() != orderedCategoryIds.size()) {
			throw new ValidationException("categoryIds does not match the requested reorder scope");
		}

		Set<Integer> targetIdSet = new HashSet<>(targetIds);
		Set<Integer> orderedIdSet = new HashSet<>(orderedCategoryIds);

		if (orderedIdSet.size() != orderedCategoryIds.size()) {
			throw new ValidationException("categoryIds contains duplicates");
		}

		if (!targetIdSet.equals(orderedIdSet)) {
			throw new ValidationException("categoryIds does not match the requested reorder scope");
		}
	}

	private Page<Category> findCategoryPage(String search, Integer page) {
		Pageable pageable = PageRequest.of(normalizePage(page), PAGE_SIZE, CATEGORY_SORT);

		return hasText(search)
				? adminCategoryRepository.findByNameContainingIgnoreCase(search.trim(), pageable)
				: adminCategoryRepository.findAll(pageable);
	}

	private int normalizePage(Integer page) {
		return page == null || page < 0 ? 0 : page;
	}

	private void normalizeSortOrders() {
		List<Category> categories = adminCategoryRepository.findAllByOrderBySortOrderAscCategoryIdAsc();
		for (int index = 0; index < categories.size(); index++) {
			categories.get(index).setSortOrder(index + 1);
		}

		adminCategoryRepository.saveAll(categories);
	}

	private void backfillSortOrderIfNeeded(Category category) {
		if (category.getSortOrder() == 0 && category.getCategoryId() != null) {
			category.setSortOrder(category.getCategoryId());
			adminCategoryRepository.save(category);
		}
	}

	private AdminCategoryResponse toResponse(Category category) {
		return new AdminCategoryResponse(
				category.getCategoryId(),
				category.getName(),
				category.getSortOrder(),
				category.getCreatedAt(),
				category.getUpdatedAt()
		);
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
