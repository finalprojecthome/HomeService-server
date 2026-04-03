package com.homeservice.homeservice_server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "supabase")
public record SupabaseAuthProperties(
		String url,
		String bucket,
		String apiKey
) {
}
