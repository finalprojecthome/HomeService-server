package com.homeservice.homeservice_server.dto;

public record AuthResponse(
		String accessToken,
		String tokenType,
		long expiresInMs
) {
}
