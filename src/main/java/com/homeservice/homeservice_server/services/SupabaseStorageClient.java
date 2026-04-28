package com.homeservice.homeservice_server.services;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.homeservice.homeservice_server.config.SupabaseProperties;
import com.homeservice.homeservice_server.dto.supabase.SupabaseUploadFileRequest;
import com.homeservice.homeservice_server.exception.BadRequestException;

@Service
public class SupabaseStorageClient {

    private final String baseUrl;
    private final RestClient client;

    SupabaseStorageClient(SupabaseProperties supabaseProperties) {
        this.baseUrl = normalizeBaseUrl(supabaseProperties.url());
        String apiKey = supabaseProperties.apiKey();
        if (this.baseUrl.isEmpty() || apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "กรุณาตั้งค่า supabase.url และ supabase.api-key (anon หรือ service role key)");
        }

        this.client = RestClient.builder()
                .baseUrl(this.baseUrl)
                .defaultHeader("apikey", apiKey)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    public String uploadPublicFile(SupabaseUploadFileRequest request) {
        MediaType mediaType = resolveMediaType(request.contentType());
        boolean upsert = request.upsert();

        try {
            client.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/storage/v1/object/{bucket}/{path}")
                            .queryParam("upsert", upsert)
                            .build(request.bucket(), request.path()))
                    .contentType(mediaType)
                    .body(request.content())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            String body = e.getResponseBodyAsString(StandardCharsets.UTF_8);
            String message = (body != null && !body.isBlank())
                    ? body
                    : "ไม่สามารถอัปโหลดไฟล์ได้ กรุณาลองใหม่อีกครั้ง";
            throw new BadRequestException(message);
        }

        return buildPublicUrl(request.bucket(), request.path());
    }

    public void deleteFile(String bucket, String path) {
        if (bucket == null || bucket.isBlank() || path == null || path.isBlank()) {
            return;
        }

        try {
            client.delete()
                    .uri("/storage/v1/object/{bucket}/{path}", bucket.trim(), path.trim())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            String body = e.getResponseBodyAsString(StandardCharsets.UTF_8);
            String lower = body == null ? "" : body.toLowerCase();
            if (e.getStatusCode().is4xxClientError()
                    && (lower.contains("not found") || lower.contains("no such file"))) {
                return;
            }
            throw new BadRequestException("ไม่สามารถลบไฟล์ได้ กรุณาลองใหม่อีกครั้ง");
        }
    }

    private static MediaType resolveMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
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

    private String buildPublicUrl(String bucket, String path) {
        String normalizedBucket = bucket == null ? "" : bucket.trim();
        String normalizedPath = path == null ? "" : path.trim();
        if (normalizedBucket.isEmpty() || normalizedPath.isEmpty()) {
            throw new IllegalArgumentException("bucket และ path ต้องไม่เป็นค่าว่าง");
        }
        return this.baseUrl + "/storage/v1/object/public/" + normalizedBucket + "/" + normalizedPath;
    }
}
