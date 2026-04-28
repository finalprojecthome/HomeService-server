package com.homeservice.homeservice_server.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.homeservice.homeservice_server.dto.user.AddressRequest;
import com.homeservice.homeservice_server.dto.user.GetAddressResponse;
import com.homeservice.homeservice_server.dto.user.UpdateProfileRequest;
import com.homeservice.homeservice_server.security.AuthRequired;
import com.homeservice.homeservice_server.security.RequestUserContext;
import com.homeservice.homeservice_server.services.AddressService;
import com.homeservice.homeservice_server.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AddressService addressService;

    @AuthRequired
    @GetMapping(value = "/addresses")
    public ResponseEntity<List<GetAddressResponse>> getAddresses(HttpServletRequest httpRequest) {
        UUID userId = (UUID) httpRequest.getAttribute(RequestUserContext.ATTR_USER_ID);
        return ResponseEntity.ok(addressService.getAddressesByUserId(userId));
    }

    @AuthRequired
    @PostMapping(value = "/addresses")
    public ResponseEntity<Map<String, String>> createAddress(
            HttpServletRequest httpRequest,
            @Valid @RequestBody AddressRequest request) {
        UUID userId = (UUID) httpRequest.getAttribute(RequestUserContext.ATTR_USER_ID);
        addressService.createAddress(userId, request);
        return ResponseEntity.ok(Map.of("message", "เพิ่มที่อยู่สำเร็จ"));
    }

    @AuthRequired
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> updateProfile(
            HttpServletRequest httpRequest,
            @Valid @ModelAttribute UpdateProfileRequest request,
            @RequestPart(name = "image", required = false) MultipartFile profileImage) {

        UUID userId = (UUID) httpRequest.getAttribute(RequestUserContext.ATTR_USER_ID);
        userService.updateProfile(userId, request, profileImage);

        return ResponseEntity.ok(Map.of("message", "แก้ไขโปรไฟล์สำเร็จ"));
    }

    @AuthRequired
    @PutMapping(value = "/addresses/{addressId}")
    public ResponseEntity<Map<String, String>> updateAddress(
            HttpServletRequest httpRequest,
            @PathVariable Integer addressId,
            @Valid @RequestBody AddressRequest request) {
        UUID userId = (UUID) httpRequest.getAttribute(RequestUserContext.ATTR_USER_ID);
        addressService.updateAddress(userId, addressId, request);
        return ResponseEntity.ok(Map.of("message", "แก้ไขที่อยู่สำเร็จ"));
    }

    @AuthRequired
    @DeleteMapping(value = "/addresses/{addressId}")
    public ResponseEntity<Map<String, String>> deleteAddress(
            HttpServletRequest httpRequest,
            @PathVariable Integer addressId) {
        UUID userId = (UUID) httpRequest.getAttribute(RequestUserContext.ATTR_USER_ID);
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok(Map.of("message", "ลบที่อยู่สำเร็จ"));
    }
}
