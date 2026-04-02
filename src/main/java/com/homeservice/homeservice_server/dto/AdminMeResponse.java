package com.homeservice.homeservice_server.dto;

import com.homeservice.homeservice_server.entity.UserRole;

import java.util.UUID;

public record AdminMeResponse(
		UUID userId,
		String email,
		String name,
		UserRole role
) {
}
