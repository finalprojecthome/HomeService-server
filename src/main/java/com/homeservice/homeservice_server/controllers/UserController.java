package com.homeservice.homeservice_server.controllers;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.homeservice.homeservice_server.dto.user.UpdateProfileRequest;
import com.homeservice.homeservice_server.security.AuthRequired;
import com.homeservice.homeservice_server.security.RequestUserContext;
import com.homeservice.homeservice_server.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @AuthRequired
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> updateProfile(
            HttpServletRequest httpRequest,
            @Valid @ModelAttribute UpdateProfileRequest request,
            @RequestPart(name = "image", required = false) MultipartFile profileImage) {

        UUID userId = (UUID) httpRequest.getAttribute(RequestUserContext.ATTR_USER_ID);
        userService.updateProfile(userId, request, profileImage);

        return ResponseEntity.ok(Map.of("message", "อัปเดตโปรไฟล์สำเร็จ"));
    }
}
