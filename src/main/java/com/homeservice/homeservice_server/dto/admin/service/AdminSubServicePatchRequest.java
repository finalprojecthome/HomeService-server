package com.homeservice.homeservice_server.dto.admin.service;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AdminSubServicePatchRequest(
		Integer subServiceId,
		@Size(max = 255)
		String name,
		@Size(max = 100)
		String unit,
		@DecimalMin(value = "0.0", inclusive = true)
		BigDecimal pricePerUnit
) {
}
