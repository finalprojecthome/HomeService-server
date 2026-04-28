package com.homeservice.homeservice_server.dto.admin.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AdminSubServiceResponse(
		Integer subServiceId,
		String name,
		String unit,
		BigDecimal pricePerUnit,
		OffsetDateTime updatedAt
) {
}
