package com.homeservice.homeservice_server.dto.admin.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminServiceUpdateRequest(
		@NotNull
		Integer categoryId,
		@NotBlank
		@Size(max = 255)
		String name,
		@Size(max = 2048)
		String imageUrl,
		@NotEmpty
		List<@Valid AdminSubServiceUpdateRequest> subServices
) {
}
