package com.homeservice.homeservice_server.dto.admin.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminServicePatchRequest(
		Integer categoryId,
		@Size(max = 255)
		String name,
		@Size(max = 2048)
		String imageUrl,
		List<@Valid AdminSubServicePatchRequest> subServices
) {
}
