package com.homeservice.homeservice_server.controller;

import com.homeservice.homeservice_server.dto.AdminCategoryCreateRequest;
import com.homeservice.homeservice_server.dto.AdminCategoryPageResponse;
import com.homeservice.homeservice_server.dto.AdminCategoryReorderRequest;
import com.homeservice.homeservice_server.dto.AdminCategoryResponse;
import com.homeservice.homeservice_server.dto.AdminCategoryUpdateRequest;
import com.homeservice.homeservice_server.service.AdminCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {
	private final AdminCategoryService adminCategoryService;

	public AdminCategoryController(AdminCategoryService adminCategoryService) {
		this.adminCategoryService = adminCategoryService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AdminCategoryResponse create(@Valid @RequestBody AdminCategoryCreateRequest request) {
		return adminCategoryService.createCategory(request.name());
	}

	@GetMapping
	public AdminCategoryPageResponse list(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) Integer page
	) {
		return adminCategoryService.getCategories(search, page);
	}

	@GetMapping("/{categoryId}")
	public AdminCategoryResponse detail(@PathVariable Integer categoryId) {
		return adminCategoryService.getCategoryById(categoryId);
	}

	@PutMapping("/{categoryId}")
	public AdminCategoryResponse update(
			@PathVariable Integer categoryId,
			@Valid @RequestBody AdminCategoryUpdateRequest request
	) {
		return adminCategoryService.updateCategory(categoryId, request.name());
	}

	@DeleteMapping("/{categoryId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(
			@PathVariable Integer categoryId,
			@RequestParam(defaultValue = "false") boolean force
	) {
		adminCategoryService.deleteCategory(categoryId, force);
	}

	@PutMapping("/reorder")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void reorder(@Valid @RequestBody AdminCategoryReorderRequest request) {
		adminCategoryService.reorderCategories(
				request.scope(),
				request.categoryIds(),
				request.search(),
				request.page()
		);
	}
}
