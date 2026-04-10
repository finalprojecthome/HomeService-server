package com.homeservice.homeservice_server.dto.service;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ServiceItemResponse {
	String id;
	String title;
	String category;
	String price;
	String imageSrc;
}
