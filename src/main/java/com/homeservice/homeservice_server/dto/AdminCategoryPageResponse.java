package com.homeservice.homeservice_server.dto;

import java.util.List;

public record AdminCategoryPageResponse(
		List<AdminCategoryResponse> items,
		int page,
		int size,
		long totalItems,
		int totalPages,
		boolean hasNext,
		boolean hasPrevious
) {
}
