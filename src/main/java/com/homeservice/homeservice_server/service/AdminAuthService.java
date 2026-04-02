package com.homeservice.homeservice_server.service;

import com.homeservice.homeservice_server.config.AdminAuthProperties;
import com.homeservice.homeservice_server.entity.User;
import com.homeservice.homeservice_server.entity.UserRole;
import com.homeservice.homeservice_server.exception.ForbiddenException;
import com.homeservice.homeservice_server.exception.UnauthorizedException;
import com.homeservice.homeservice_server.exception.ValidationException;
import com.homeservice.homeservice_server.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class AdminAuthService {
	private final AdminAuthProperties adminAuthProperties;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AdminAuthService(
			AdminAuthProperties adminAuthProperties,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService
	) {
		this.adminAuthProperties = adminAuthProperties;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional
	public String register(String name, String phone, String email, String rawPassword, String inviteCode) {
		String configuredInvite = adminAuthProperties.inviteCode();
		if (configuredInvite == null || configuredInvite.isBlank()) {
			throw new ForbiddenException("Admin registration is disabled");
		}
		if (inviteCode == null || !configuredInvite.equals(inviteCode)) {
			throw new ForbiddenException("Invalid invite code");
		}

		if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
			throw new ValidationException("Email already exists");
		}

		User user = User.builder()
				.name(name)
				.phone(phone)
				.email(email)
				.role(UserRole.ADMIN)
				.password(passwordEncoder.encode(rawPassword))
				.build();

		userRepository.save(user);
		return jwtService.generateAccessToken(user.getEmail(), user.getRole());
	}

	@Transactional
	public String login(String email, String rawPassword) {
		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

		if (user.getRole() != UserRole.ADMIN) {
			throw new ForbiddenException("Not an admin");
		}
		if (user.getPassword() == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
			throw new UnauthorizedException("Invalid credentials");
		}

		user.setLastLoginAt(OffsetDateTime.now());
		return jwtService.generateAccessToken(user.getEmail(), user.getRole());
	}
}
