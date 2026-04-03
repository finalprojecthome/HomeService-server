package com.homeservice.homeservice_server.dto;

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
