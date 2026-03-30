package com.homeservice.homeservice_server.dto.auth;

import com.homeservice.homeservice_server.validation.RegistrationRole;
import com.homeservice.homeservice_server.validation.ValidationPatterns;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Pattern(regexp = ValidationPatterns.FULL_NAME_PATTERN, message = "Invalid full name")
    private String fullname;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = ValidationPatterns.PHONE_PATTERN, message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 12, message = "Password must be at least 12 characters long")
    private String password;

    @NotBlank(message = "Role is required")
    @RegistrationRole
    private String role;
}
