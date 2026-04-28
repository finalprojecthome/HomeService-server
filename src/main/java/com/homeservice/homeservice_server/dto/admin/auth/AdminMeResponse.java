package com.homeservice.homeservice_server.dto.admin.auth;

import java.util.UUID;

public record AdminMeResponse(
		UUID userId,
		String email,
		String name,
		String role
) {
}
