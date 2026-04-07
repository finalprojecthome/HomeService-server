package com.homeservice.homeservice_server.dto.admin.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminRegisterRequest(
		@NotBlank @Size(max = 255) String name,
		@NotBlank @Size(max = 50) String phone,
		@NotBlank @Email @Size(max = 255) String email,
		@NotBlank @Size(min = 8, max = 72) String password,
		@NotBlank String inviteCode
) {
}
