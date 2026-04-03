package com.homeservice.homeservice_server.service;

import com.homeservice.homeservice_server.config.SupabaseAuthProperties;
import com.homeservice.homeservice_server.dto.supabase.SupabaseGetUserResponse;
import com.homeservice.homeservice_server.dto.supabase.SupabaseLoginResponse;
import com.homeservice.homeservice_server.dto.supabase.SupabaseRegisterResponse;
import com.homeservice.homeservice_server.exception.ConflictException;
import com.homeservice.homeservice_server.exception.UnauthorizedException;
import com.homeservice.homeservice_server.exception.ValidationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Service
public class SupabaseAuthClient {
	private final RestClient client;

	public SupabaseAuthClient(SupabaseAuthProperties supabaseAuthProperties) {
		String baseUrl = normalizeBaseUrl(supabaseAuthProperties.url());
		String apiKey = supabaseAuthProperties.apiKey();
		if (baseUrl.isEmpty() || apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException("กรุณาตั้งค่า supabase.url และ supabase.apiKey");
		}

		this.client = RestClient.builder()
				.baseUrl(baseUrl)
				.defaultHeader("apikey", apiKey)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.build();
	}

	public UUID signUp(String email, String password) {
		try {
			SupabaseRegisterResponse response = client.post()
					.uri("/auth/v1/signup")
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of("email", email, "password", password))
					.retrieve()
					.body(SupabaseRegisterResponse.class);

			if (response == null || response.user() == null || response.user().id() == null || response.user().id().isBlank()) {
				throw new ValidationException("ไม่สามารถสร้างผู้ใช้งานได้ กรุณาลองใหม่อีกครั้ง");
			}
			return UUID.fromString(response.user().id());
		} catch (RestClientResponseException e) {
			throw mapSignUpFailure(e);
		} catch (IllegalArgumentException e) {
			throw new ValidationException("ไม่สามารถสร้างผู้ใช้งานได้ กรุณาลองใหม่อีกครั้ง");
		}
	}

	public SupabaseLoginResponse signIn(String email, String password) {
		try {
			SupabaseLoginResponse response = client.post()
					.uri("/auth/v1/token?grant_type=password")
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of("email", email, "password", password))
					.retrieve()
					.body(SupabaseLoginResponse.class);

			if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
				throw new UnauthorizedException("Invalid credentials");
			}
			return response;
		} catch (RestClientResponseException e) {
			throw new UnauthorizedException("Invalid credentials");
		}
	}

	public SupabaseGetUserResponse getUser(String accessToken) {
		try {
			SupabaseGetUserResponse response = client.get()
					.uri("/auth/v1/user")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.retrieve()
					.body(SupabaseGetUserResponse.class);

			if (response == null || response.id() == null || response.id().isBlank()) {
				throw new UnauthorizedException("Invalid token");
			}
			return response;
		} catch (RestClientResponseException e) {
			throw new UnauthorizedException("Invalid token");
		}
	}

	private RuntimeException mapSignUpFailure(RestClientResponseException e) {
		String raw = e.getResponseBodyAsString(StandardCharsets.UTF_8);
		if (raw != null) {
			String lower = raw.toLowerCase();
			if (lower.contains("user_already_exists")
					|| lower.contains("user already registered")
					|| lower.contains("email address is already registered")) {
				return new ConflictException("Email already exists");
			}
		}
		return new ValidationException("ไม่สามารถสร้างผู้ใช้งานได้ กรุณาลองใหม่อีกครั้ง");
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
