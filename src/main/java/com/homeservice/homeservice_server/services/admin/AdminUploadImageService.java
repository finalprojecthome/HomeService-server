package com.homeservice.homeservice_server.services.admin;

import com.homeservice.homeservice_server.config.SupabaseProperties;
import com.homeservice.homeservice_server.dto.admin.service.AdminUploadImageResponse;
import com.homeservice.homeservice_server.exception.BadRequestException;
import com.homeservice.homeservice_server.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class AdminUploadImageService {
	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/jpg");

	private final RestClient client;
	private final String supabaseBaseUrl;
	private final AdminServiceUploadProperties adminServiceUploadProperties;

	@Autowired
	public AdminUploadImageService(
			SupabaseProperties supabaseProperties,
			AdminServiceUploadProperties adminServiceUploadProperties
	) {
		this(RestClient.builder(), supabaseProperties, adminServiceUploadProperties);
	}

	AdminUploadImageService(
			RestClient.Builder restClientBuilder,
			SupabaseProperties supabaseProperties,
			AdminServiceUploadProperties adminServiceUploadProperties
	) {
		this.supabaseBaseUrl = normalizeBaseUrl(supabaseProperties.url());
		String apiKey = normalizeText(supabaseProperties.apiKey());
		String bucketName = normalizeText(adminServiceUploadProperties.bucketName());
		if (supabaseBaseUrl.isEmpty() || apiKey.isEmpty()) {
			throw new IllegalStateException("Please configure supabase.url and supabase.api-key");
		}
		if (bucketName.isEmpty()) {
			throw new IllegalStateException("Please configure supabase.bucket.adminservice");
		}

		this.client = restClientBuilder
				.baseUrl(supabaseBaseUrl)
				.defaultHeader("apikey", apiKey)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.build();
		this.adminServiceUploadProperties = adminServiceUploadProperties;
	}

	public AdminUploadImageResponse uploadServiceImage(MultipartFile image) {
		validateImage(image);

		String contentType = normalizeContentType(image.getContentType());
		String objectPath = "services/" + UUID.randomUUID() + "." + resolveFileExtension(contentType);

		try {
			client.post()
					.uri("/storage/v1/object/%s/%s".formatted(
							encodePathSegment(adminServiceUploadProperties.bucketName()),
							encodeObjectPath(objectPath)
					))
					.contentType(MediaType.parseMediaType(contentType))
					.body(image.getBytes())
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientResponseException ex) {
			throw new BadRequestException("Unable to upload service image");
		} catch (IOException ex) {
			throw new BadRequestException("Unable to read image file");
		}

		return new AdminUploadImageResponse(buildPublicImageUrl(objectPath));
	}

	private void validateImage(MultipartFile image) {
		if (image == null) {
			throw new ValidationException("Image file is required");
		}
		if (image.isEmpty()) {
			throw new ValidationException("Image file must not be empty");
		}
		String contentType = normalizeContentType(image.getContentType());
		if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new ValidationException("Image must be image/jpeg, image/png, or image/jpg");
		}
		if (image.getSize() > adminServiceUploadProperties.maxFileSizeBytes()) {
			throw new ValidationException("Image size must not exceed 10MB");
		}
	}

	private String buildPublicImageUrl(String objectPath) {
		return "%s/storage/v1/object/public/%s/%s".formatted(
				supabaseBaseUrl,
				encodePathSegment(adminServiceUploadProperties.bucketName()),
				encodeObjectPath(objectPath)
		);
	}

	private String resolveFileExtension(String contentType) {
		return switch (contentType) {
			case "image/png" -> "png";
			case "image/jpg" -> "jpg";
			case "image/jpeg" -> "jpeg";
			default -> throw new ValidationException("Image must be image/jpeg, image/png, or image/jpg");
		};
	}

	private String normalizeContentType(String rawContentType) {
		return normalizeText(rawContentType).toLowerCase(Locale.ROOT);
	}

	private static String normalizeBaseUrl(String url) {
		String normalized = normalizeText(url);
		while (normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		return normalized;
	}

	private static String normalizeText(String value) {
		return value == null ? "" : value.trim();
	}

	private static String encodePathSegment(String value) {
		return UriUtils.encodePathSegment(value, StandardCharsets.UTF_8);
	}

	private static String encodeObjectPath(String value) {
		return UriUtils.encodePath(value, StandardCharsets.UTF_8);
	}
}
