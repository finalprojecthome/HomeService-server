package com.homeservice.homeservice_server.dto.admin.service;

import java.util.List;

public record AdminServicePageResponse(
		List<AdminServiceResponse> items,
		int page,
		int size,
		long totalItems,
		int totalPages,
		boolean hasNext,
		boolean hasPrevious
) {
}
