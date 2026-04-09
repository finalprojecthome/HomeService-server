package com.homeservice.homeservice_server.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.homeservice.homeservice_server.dto.auth.GetUserResponse;
import com.homeservice.homeservice_server.dto.auth.LoginRequest;
import com.homeservice.homeservice_server.dto.auth.LoginResponse;
import com.homeservice.homeservice_server.dto.auth.RegisterRequest;
import com.homeservice.homeservice_server.dto.auth.ResetPasswordRequest;
import com.homeservice.homeservice_server.security.AuthRequired;
import com.homeservice.homeservice_server.services.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @AuthRequired
    @GetMapping("/get-user")
    public ResponseEntity<GetUserResponse> getUser(
            @RequestHeader("Authorization") String authorization) {

        String accessToken = authorization.replaceFirst("(?i)^Bearer\\s+", "").trim();

        GetUserResponse response = authService.getUser(accessToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(201).body(Map.of("message", "ลงทะเบียนสำเร็จ"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @AuthRequired
    @PutMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ResetPasswordRequest request) {

        String accessToken = authorization.replaceFirst("(?i)^Bearer\\s+", "").trim();
        authService.resetPassword(accessToken, request);
        return ResponseEntity.ok(Map.of("message", "เปลี่ยนรหัสผ่านสำเร็จ"));
    }
}
