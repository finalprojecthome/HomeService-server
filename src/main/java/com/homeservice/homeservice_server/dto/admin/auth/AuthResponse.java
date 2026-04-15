package com.homeservice.homeservice_server.dto.admin.auth;

public record AuthResponse(
		String accessToken,
		String tokenType,
		long expiresInMs
) {
}
