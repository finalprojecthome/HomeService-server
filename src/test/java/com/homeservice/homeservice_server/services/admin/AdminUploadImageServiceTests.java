package com.homeservice.homeservice_server.services.admin;

import com.homeservice.homeservice_server.config.SupabaseProperties;
import com.homeservice.homeservice_server.dto.admin.service.AdminUploadImageResponse;
import com.homeservice.homeservice_server.exception.BadRequestException;
import com.homeservice.homeservice_server.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AdminUploadImageServiceTests {

	@Test
	void uploadServiceImage_withAllowedMimeTypes_usesAdminServiceBucketAndReturnsPublicUrl() {
		verifySuccessfulUpload("image/png", "png");
		verifySuccessfulUpload("image/jpeg", "jpeg");
		verifySuccessfulUpload("image/jpg", "jpg");
	}

	@Test
	void uploadServiceImage_withUnsupportedMimeType_throwsValidationException() {
		AdminUploadImageService service = createService(RestClient.builder());

		ValidationException exception = assertThrows(ValidationException.class, () ->
				service.uploadServiceImage(new MockMultipartFile("image", "service.gif", "image/gif", new byte[]{1, 2, 3})));

		assertEquals("Image must be image/jpeg, image/png, or image/jpg", exception.getMessage());
	}

	@Test
	void uploadServiceImage_withFileTooLarge_throwsValidationException() {
		AdminUploadImageService service = createService(RestClient.builder());
		byte[] oversized = new byte[10 * 1024 * 1024 + 1];

		ValidationException exception = assertThrows(ValidationException.class, () ->
				service.uploadServiceImage(new MockMultipartFile("image", "service.png", "image/png", oversized)));

		assertEquals("Image size must not exceed 10MB", exception.getMessage());
	}

	@Test
	void uploadServiceImage_whenSupabaseUploadFails_throwsBadRequestException() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		AdminUploadImageService service = createService(builder);

		server.expect(once(), request -> {
					assertTrue(request.getURI().getPath().startsWith("/storage/v1/object/admin-service/services/"));
				})
				.andExpect(method(HttpMethod.POST))
				.andRespond(withBadRequest());

		BadRequestException exception = assertThrows(BadRequestException.class, () ->
				service.uploadServiceImage(new MockMultipartFile("image", "service.png", "image/png", new byte[]{1, 2, 3})));

		assertEquals("Unable to upload service image", exception.getMessage());
		server.verify();
	}

	private void verifySuccessfulUpload(String contentType, String expectedExtension) {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		AdminUploadImageService service = createService(builder);

		server.expect(once(), request -> {
					String path = request.getURI().getPath();
					assertTrue(path.startsWith("/storage/v1/object/admin-service/services/"));
					assertTrue(path.endsWith("." + expectedExtension));
				})
				.andExpect(method(HttpMethod.POST))
				.andExpect(header("apikey", "service-key"))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer service-key"))
				.andExpect(header(HttpHeaders.CONTENT_TYPE, contentType))
				.andRespond(withSuccess("", MediaType.APPLICATION_JSON));

		AdminUploadImageResponse response = service.uploadServiceImage(
				new MockMultipartFile("image", "service." + expectedExtension, contentType, new byte[]{1, 2, 3}));

		assertTrue(response.imageUrl().startsWith("https://project.supabase.co/storage/v1/object/public/admin-service/services/"));
		assertTrue(response.imageUrl().endsWith("." + expectedExtension));
		server.verify();
	}

	private AdminUploadImageService createService(RestClient.Builder builder) {
		return new AdminUploadImageService(
				builder,
				new SupabaseProperties("https://project.supabase.co", "service-key"),
				new AdminServiceUploadProperties("admin-service", 10 * 1024 * 1024L)
		);
	}
}
