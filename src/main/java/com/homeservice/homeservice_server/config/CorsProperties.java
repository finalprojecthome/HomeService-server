package com.homeservice.homeservice_server.config;

import java.util.Arrays;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(String allowedOrigins) {

    public String[] allowedOriginArray() {
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            return new String[0];
        }
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }
}
