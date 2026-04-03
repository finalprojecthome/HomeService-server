package com.homeservice.homeservice_server.dto.supabase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SupabaseRegisterResponse(
		@JsonProperty("user") SupabaseAuthUser user) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record SupabaseAuthUser(
			String id,
			String email) {
	}
}
