package com.homeservice.homeservice_server.dto.supabase;

public record SupabaseUploadFileRequest(
        String bucket,
        String path,
        byte[] content,
        String contentType,
        boolean upsert) {
}
