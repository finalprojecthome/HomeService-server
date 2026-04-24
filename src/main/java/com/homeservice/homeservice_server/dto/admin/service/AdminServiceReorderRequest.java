package com.homeservice.homeservice_server.dto.admin.service;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AdminServiceReorderRequest(
		String scope,
		@NotEmpty
		List<Integer> serviceIds,
		String search,
		Integer categoryId,
		Integer page
) {
}
