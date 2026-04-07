package com.homeservice.homeservice_server.dto.admin.service;

import java.time.OffsetDateTime;
import java.util.List;

public record AdminServiceResponse(
		Integer serviceId,
		Integer categoryId,
		String name,
		String imageUrl,
		Integer sortOrder,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt,
		List<AdminSubServiceResponse> subServices
) {
}
