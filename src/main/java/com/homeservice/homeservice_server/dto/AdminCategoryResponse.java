package com.homeservice.homeservice_server.dto;

import java.time.OffsetDateTime;

public record AdminCategoryResponse(
		Integer categoryId,
		String name,
		Integer sortOrder,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
