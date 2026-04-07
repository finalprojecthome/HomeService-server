package com.homeservice.homeservice_server.dto;

public record ServiceCatalogItemResponse(
		String id,
		String title,
		String category,
		String price,
		String imageSrc
) {
}
