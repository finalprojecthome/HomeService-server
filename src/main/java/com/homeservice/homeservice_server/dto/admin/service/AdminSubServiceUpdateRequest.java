package com.homeservice.homeservice_server.dto.admin.service;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AdminSubServiceUpdateRequest(
		Integer subServiceId,
		@NotBlank
		@Size(max = 255)
		String name,
		@NotBlank
		@Size(max = 100)
		String unit,
		@NotNull
		@DecimalMin(value = "0.0", inclusive = true)
		BigDecimal pricePerUnit
) {
}
