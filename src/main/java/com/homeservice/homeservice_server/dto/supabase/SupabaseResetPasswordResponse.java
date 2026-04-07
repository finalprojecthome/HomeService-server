package com.homeservice.homeservice_server.dto.supabase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SupabaseResetPasswordResponse(String id, String email) {
}
