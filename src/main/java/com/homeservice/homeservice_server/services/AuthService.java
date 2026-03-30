package com.homeservice.homeservice_server.services;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homeservice.homeservice_server.dto.auth.GetUserResponse;
import com.homeservice.homeservice_server.dto.auth.LoginRequest;
import com.homeservice.homeservice_server.dto.auth.LoginResponse;
import com.homeservice.homeservice_server.dto.auth.RegisterRequest;
import com.homeservice.homeservice_server.entities.User;
import com.homeservice.homeservice_server.enums.UserRole;
import com.homeservice.homeservice_server.exception.BadRequestException;
import com.homeservice.homeservice_server.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SupabaseAuthClient supabaseAuthClient;

    @Transactional
    public void register(RegisterRequest request) {
        String phone = request.getPhone();
        String email = request.getEmail();

        if (userRepository.existsByPhone(phone)) {
            throw new BadRequestException("User with this phone number already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("User with this email already exists");
        }

        UUID authUserId = supabaseAuthClient.signUp(email, request.getPassword());

        UserRole role = UserRole.fromDb(request.getRole().trim());

        User user = User.builder()
                .userId(authUserId)
                .name(request.getFullname())
                .phone(phone)
                .email(email)
                .role(role)
                .build();

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        var supabaseResponse = supabaseAuthClient.signIn(request.getEmail(), request.getPassword());

        return LoginResponse.builder()
                .message("Login successfully")
                .accessToken(supabaseResponse.accessToken())
                .build();
    }

    public GetUserResponse getUser(String accessToken) {
        var supaUser = supabaseAuthClient.getUser(accessToken);

        UUID userId = UUID.fromString(supaUser.id());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        return GetUserResponse.builder()
                .id(user.getUserId().toString())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .imgUrl(user.getImgUrl())
                .role(user.getRole())
                .build();
    }
}
