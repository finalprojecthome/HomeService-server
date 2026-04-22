package com.homeservice.homeservice_server.dto.admin.category;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AdminCategoryReorderRequest(
		String scope,
		@NotEmpty
		List<Integer> categoryIds,
		String search,
		Integer page
) {
}
