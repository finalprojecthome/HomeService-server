package com.homeservice.homeservice_server.service;

import com.homeservice.homeservice_server.config.AdminAuthProperties;
import com.homeservice.homeservice_server.dto.AuthResponse;
import com.homeservice.homeservice_server.dto.AdminMeResponse;
import com.homeservice.homeservice_server.dto.supabase.SupabaseLoginResponse;
import com.homeservice.homeservice_server.entity.User;
import com.homeservice.homeservice_server.entity.UserRole;
import com.homeservice.homeservice_server.exception.ForbiddenException;
import com.homeservice.homeservice_server.exception.UnauthorizedException;
import com.homeservice.homeservice_server.exception.ValidationException;
import com.homeservice.homeservice_server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AdminAuthService {
	private final AdminAuthProperties adminAuthProperties;
	private final UserRepository userRepository;
	private final SupabaseAuthClient supabaseAuthClient;

	public AdminAuthService(
			AdminAuthProperties adminAuthProperties,
			UserRepository userRepository,
			SupabaseAuthClient supabaseAuthClient
	) {
		this.adminAuthProperties = adminAuthProperties;
		this.userRepository = userRepository;
		this.supabaseAuthClient = supabaseAuthClient;
	}

	@Transactional
	public AuthResponse register(
			String name,
			String phone,
			String email,
			String password,
			String inviteCode
	) {
		String configuredInvite = adminAuthProperties.inviteCode();
		if (configuredInvite == null || configuredInvite.isBlank()) {
			throw new ForbiddenException("Admin registration is disabled");
		}
		if (inviteCode == null || !configuredInvite.equals(inviteCode)) {
			throw new ForbiddenException("Invalid invite code");
		}

		String normalizedEmail = email.trim().toLowerCase();
		if (password == null || password.isBlank()) {
			throw new ValidationException("Password is required");
		}

		Optional<User> existingByEmail = userRepository.findByEmailIgnoreCase(normalizedEmail);
		UUID authUserId = supabaseAuthClient.signUp(normalizedEmail, password);

		if (existingByEmail.isPresent() && !existingByEmail.get().getUserId().equals(authUserId)) {
			migrateUserIdentity(existingByEmail.get(), authUserId, normalizedEmail, name, phone);
		}

		User user = userRepository.findById(authUserId)
				.orElseGet(() -> User.builder().userId(authUserId).build());
		user.setEmail(normalizedEmail);
		user.setName(name.trim());
		user.setPhone(phone.trim());
		user.setRole(UserRole.ADMIN);
		userRepository.save(user);

		SupabaseLoginResponse loginResponse = supabaseAuthClient.signIn(normalizedEmail, password);
		return new AuthResponse(
				loginResponse.accessToken(),
				loginResponse.tokenType(),
				toMillis(loginResponse.expiresIn())
		);
	}

	public AuthResponse login(String email, String password) {
		String normalizedEmail = email.trim().toLowerCase();
		User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
				.orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
		if (user.getRole() != UserRole.ADMIN) {
			throw new ForbiddenException("Not an admin");
		}

		SupabaseLoginResponse loginResponse = supabaseAuthClient.signIn(normalizedEmail, password);
		return new AuthResponse(
				loginResponse.accessToken(),
				loginResponse.tokenType(),
				toMillis(loginResponse.expiresIn())
		);
	}

	private void migrateUserIdentity(User existingUser, UUID targetUserId, String email, String name, String phone) {
		User migrated = User.builder()
				.userId(targetUserId)
				.name(name.trim().isEmpty() ? existingUser.getName() : name.trim())
				.phone(phone.trim().isEmpty() ? existingUser.getPhone() : phone.trim())
				.email(email)
				.imgUrl(existingUser.getImgUrl())
				.role(UserRole.ADMIN)
				.password(existingUser.getPassword())
				.lastLoginAt(existingUser.getLastLoginAt())
				.createdAt(existingUser.getCreatedAt())
				.build();
		userRepository.delete(existingUser);
		userRepository.save(migrated);
	}

	private long toMillis(Long expiresInSeconds) {
		if (expiresInSeconds == null) {
			return 0L;
		}
		return expiresInSeconds * 1000L;
	}
}
