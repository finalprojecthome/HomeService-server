package com.homeservice.homeservice_server.config;

import java.util.UUID;

public record SupabaseUserPrincipal(
		UUID userId,
		String email
) {
}
