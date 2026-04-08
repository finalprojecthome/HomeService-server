package com.homeservice.homeservice_server.services;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homeservice.homeservice_server.dto.auth.GetUserResponse;
import com.homeservice.homeservice_server.dto.auth.LoginRequest;
import com.homeservice.homeservice_server.dto.auth.LoginResponse;
import com.homeservice.homeservice_server.dto.auth.RegisterRequest;
import com.homeservice.homeservice_server.dto.auth.ResetPasswordRequest;
import com.homeservice.homeservice_server.entities.Technician;
import com.homeservice.homeservice_server.entities.User;
import com.homeservice.homeservice_server.enums.UserRole;
import com.homeservice.homeservice_server.exception.BadRequestException;
import com.homeservice.homeservice_server.exception.ConflictException;
import com.homeservice.homeservice_server.exception.NotFoundException;
import com.homeservice.homeservice_server.repositories.TechnicianRepository;
import com.homeservice.homeservice_server.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TechnicianRepository technicianRepository;
    private final SupabaseAuthClient supabaseAuthClient;

    @Transactional
    public void register(RegisterRequest request) {
        String phone = request.getPhone();
        String email = request.getEmail();

        if (userRepository.existsByPhone(phone)) {
            throw new ConflictException("มีผู้ใช้งานที่ใช้เบอร์โทรศัพท์นี้อยู่แล้ว");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("มีผู้ใช้งานที่ใช้อีเมลนี้อยู่แล้ว");
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

        User savedUser = userRepository.save(user);

        if (role == UserRole.TECHNICIAN) {
            technicianRepository.save(Technician.builder()
                    .user(savedUser)
                    .build());
        }
    }

    public LoginResponse login(LoginRequest request) {
        var supabaseResponse = supabaseAuthClient.signIn(request.getEmail(), request.getPassword());

        return LoginResponse.builder()
                .message("เข้าสู่ระบบสำเร็จ")
                .accessToken(supabaseResponse.accessToken())
                .build();
    }

    public GetUserResponse getUser(String accessToken) {
        var supaUser = supabaseAuthClient.getUser(accessToken);

        UUID userId = UUID.fromString(supaUser.id());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("ไม่พบผู้ใช้งาน"));

        return GetUserResponse.builder()
                .id(user.getUserId().toString())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .imgUrl(user.getImgUrl())
                .role(user.getRole())
                .updatedAt(user.getUpdatedAt())
                .build();

    }
    public UUID getCurrentUserId(String accessToken) {
        var supaUser = supabaseAuthClient.getUser(accessToken);
        return UUID.fromString(supaUser.id());
    }

    public void resetPassword(String accessToken, ResetPasswordRequest request) {
        if (request.getNewPassword().equals(request.getOldPassword())) {
            throw new BadRequestException("รหัสผ่านใหม่ต้องไม่ซ้ำกับรหัสผ่านเดิม");
        }

        GetUserResponse current = getUser(accessToken);
        var verifiedSession = supabaseAuthClient.signIn(current.getEmail(), request.getOldPassword(), true);
        supabaseAuthClient.updatePassword(verifiedSession.accessToken(), request.getNewPassword());
    }

}
