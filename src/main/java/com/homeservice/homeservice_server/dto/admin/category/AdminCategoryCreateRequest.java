package com.homeservice.homeservice_server.dto.admin.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCategoryCreateRequest(
		@NotBlank
		@Size(max = 255)
		String name
) {
}
