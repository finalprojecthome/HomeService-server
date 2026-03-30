package com.homeservice.homeservice_server.services;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.homeservice.homeservice_server.config.SupabaseProperties;
import com.homeservice.homeservice_server.dto.supabase.SupabaseRegisterResponse;
import com.homeservice.homeservice_server.exception.BadRequestException;

@Service
public class SupabaseAuthClient {

    private final RestClient client;

    public SupabaseAuthClient(SupabaseProperties supabaseProperties) {
        String baseUrl = normalizeBaseUrl(supabaseProperties.url());
        String apiKey = supabaseProperties.apiKey();
        if (baseUrl.isEmpty() || apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Configure supabase.url and supabase.api-key (anon or service role key).");
        }

        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("apikey", apiKey)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    /**
     * Calls Supabase GoTrue {@code POST /auth/v1/signup}.
     *
     * @return {@code auth.users.id} for the new user
     */
    public UUID signUp(String email, String password) {
        Map<String, String> body = Map.of(
                "email", email,
                "password", password);

        try {
            SupabaseRegisterResponse response = client.post()
                    .uri("/auth/v1/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(SupabaseRegisterResponse.class);

            if (response == null
                    || response.user() == null
                    || response.user().id() == null
                    || response.user().id().isBlank()) {
                throw new BadRequestException("Failed to create user. Please try again");
            }
            return UUID.fromString(response.user().id());
        } catch (RestClientResponseException e) {
            throw mapSignUpFailure(e);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Failed to create user. Please try again");
        }
    }

    private BadRequestException mapSignUpFailure(RestClientResponseException e) {
        String raw = e.getResponseBodyAsString(StandardCharsets.UTF_8);
        if (raw != null) {
            String lower = raw.toLowerCase();
            if (lower.contains("user_already_exists")
                    || lower.contains("user already registered")
                    || lower.contains("email address is already registered")) {
                return new BadRequestException("User with this email already exists");
            }
        }
        return new BadRequestException("Failed to create user. Please try again");
    }

    private static String normalizeBaseUrl(String url) {
        if (url == null) {
            return "";
        }
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
