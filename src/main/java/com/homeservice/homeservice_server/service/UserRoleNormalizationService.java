package com.homeservice.homeservice_server.service;

import com.homeservice.homeservice_server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRoleNormalizationService {
	private final UserRepository userRepository;

	public UserRoleNormalizationService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional
	public int normalizeStoredRolesToLowercase() {
		return userRepository.normalizeStoredRolesToLowercase();
	}
}
