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
import com.homeservice.homeservice_server.dto.supabase.SupabaseGetUserResponse;
import com.homeservice.homeservice_server.dto.supabase.SupabaseLoginResponse;
import com.homeservice.homeservice_server.dto.supabase.SupabaseRegisterResponse;
import com.homeservice.homeservice_server.exception.BadRequestException;
import com.homeservice.homeservice_server.exception.ConflictException;
import com.homeservice.homeservice_server.exception.UnauthorizedException;

@Service
public class SupabaseAuthClient {

    private final RestClient client;

    private SupabaseAuthClient(SupabaseProperties supabaseProperties) {
        String baseUrl = normalizeBaseUrl(supabaseProperties.url());
        String apiKey = supabaseProperties.apiKey();
        if (baseUrl.isEmpty() || apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "กรุณาตั้งค่า supabase.url และ supabase.api-key (anon หรือ service role key)");
        }

        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("apikey", apiKey)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

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
                throw new BadRequestException("ไม่สามารถสร้างผู้ใช้งานได้ กรุณาลองใหม่อีกครั้ง");
            }
            return UUID.fromString(response.user().id());
        } catch (RestClientResponseException e) {
            throw mapSignUpFailure(e);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("ไม่สามารถสร้างผู้ใช้งานได้ กรุณาลองใหม่อีกครั้ง");
        }
    }

    public SupabaseLoginResponse signIn(String email, String password) {
        Map<String, String> body = Map.of(
                "email", email,
                "password", password);

        try {
            SupabaseLoginResponse response = client.post()
                    .uri("/auth/v1/token?grant_type=password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(SupabaseLoginResponse.class);

            if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
                throw new UnauthorizedException("อีเมลหรือรหัสผ่านไม่ถูกต้อง");
            }
            return response;
        } catch (RestClientResponseException e) {
            throw new UnauthorizedException("อีเมลหรือรหัสผ่านไม่ถูกต้อง");
        }
    }

    public SupabaseGetUserResponse getUser(String accessToken) {
        try {
            SupabaseGetUserResponse response = client.get()
                    .uri("/auth/v1/user")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(SupabaseGetUserResponse.class);

            if (response == null || response.id() == null) {
                throw new UnauthorizedException("โทเคนไม่ถูกต้อง");
            }
            return response;
        } catch (RestClientResponseException e) {
            throw new UnauthorizedException("โทเคนไม่ถูกต้อง");
        }
    }

    private RuntimeException mapSignUpFailure(RestClientResponseException e) {
        String raw = e.getResponseBodyAsString(StandardCharsets.UTF_8);
        if (raw != null) {
            String lower = raw.toLowerCase();
            if (lower.contains("user_already_exists")
                    || lower.contains("user already registered")
                    || lower.contains("email address is already registered")) {
                return new ConflictException("มีผู้ใช้งานที่ใช้อีเมลนี้อยู่แล้ว");
            }
        }
        return new BadRequestException("ไม่สามารถสร้างผู้ใช้งานได้ กรุณาลองใหม่อีกครั้ง");
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
