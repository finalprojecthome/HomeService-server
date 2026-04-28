package com.homeservice.homeservice_server.controllers.admin;

import com.homeservice.homeservice_server.config.SupabaseUserPrincipal;
import com.homeservice.homeservice_server.dto.admin.auth.AdminLoginRequest;
import com.homeservice.homeservice_server.dto.admin.auth.AdminMeResponse;
import com.homeservice.homeservice_server.dto.admin.auth.AdminRegisterRequest;
import com.homeservice.homeservice_server.dto.admin.auth.AuthResponse;
import com.homeservice.homeservice_server.entities.User;
import com.homeservice.homeservice_server.exception.UnauthorizedException;
import com.homeservice.homeservice_server.repositories.UserRepository;
import com.homeservice.homeservice_server.services.admin.AdminAuthService;
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

	public AdminAuthController(
			AdminAuthService adminAuthService,
			UserRepository userRepository
	) {
		this.adminAuthService = adminAuthService;
		this.userRepository = userRepository;
	}

	@PostMapping("/register")
	public AuthResponse register(@Valid @RequestBody AdminRegisterRequest request) {
		return adminAuthService.register(
				request.name(),
				request.phone(),
				request.email(),
				request.password(),
				request.inviteCode()
		);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody AdminLoginRequest request) {
		return adminAuthService.login(request.email(), request.password());
	}

	@GetMapping("/me")
	public AdminMeResponse me(Authentication authentication) {
		SupabaseUserPrincipal principal = requirePrincipal(authentication);
		User user = userRepository.findById(principal.userId())
				.orElseThrow(() -> new UnauthorizedException("Unauthenticated"));
		return new AdminMeResponse(
				user.getUserId(),
				user.getEmail(),
				user.getName(),
				user.getRole().getDbValue()
		);
	}

	private SupabaseUserPrincipal requirePrincipal(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof SupabaseUserPrincipal principal)) {
			throw new UnauthorizedException("Unauthenticated");
		}
		return principal;
	}
}
