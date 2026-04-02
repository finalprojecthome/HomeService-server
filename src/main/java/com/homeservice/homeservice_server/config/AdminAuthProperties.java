package com.homeservice.homeservice_server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin")
public record AdminAuthProperties(
		String inviteCode
) {
}
