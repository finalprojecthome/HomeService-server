package com.homeservice.homeservice_server.controller;

import com.homeservice.homeservice_server.dto.AdminLoginRequest;
import com.homeservice.homeservice_server.dto.AdminMeResponse;
import com.homeservice.homeservice_server.dto.AdminRegisterRequest;
import com.homeservice.homeservice_server.dto.AuthResponse;
import com.homeservice.homeservice_server.config.JwtProperties;
import com.homeservice.homeservice_server.entity.User;
import com.homeservice.homeservice_server.exception.UnauthorizedException;
import com.homeservice.homeservice_server.repository.UserRepository;
import com.homeservice.homeservice_server.service.AdminAuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {
	private final AdminAuthService adminAuthService;
	private final UserRepository userRepository;
	private final long jwtExpirationMs;

	public AdminAuthController(
			AdminAuthService adminAuthService,
			UserRepository userRepository,
			JwtProperties jwtProperties
	) {
		this.adminAuthService = adminAuthService;
		this.userRepository = userRepository;
		this.jwtExpirationMs = jwtProperties.expiration();
	}

	@PostMapping("/register")
	public AuthResponse register(@Valid @RequestBody AdminRegisterRequest request) {
		String token = adminAuthService.register(
				request.name(),
				request.phone(),
				request.email(),
				request.password(),
				request.inviteCode()
		);
		return new AuthResponse(token, "Bearer", jwtExpirationMs);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody AdminLoginRequest request) {
		String token = adminAuthService.login(request.email(), request.password());
		return new AuthResponse(token, "Bearer", jwtExpirationMs);
	}

	@GetMapping("/me")
	public AdminMeResponse me(Authentication authentication) {
		if (authentication == null || authentication.getName() == null) {
			throw new UnauthorizedException("Unauthenticated");
		}
		String email = authentication.getName();
		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new UnauthorizedException("Unauthenticated"));
		return new AdminMeResponse(
				user.getUserId(),
				user.getEmail(),
				user.getName(),
				user.getRole().toExternalValue()
		);
	}
}
