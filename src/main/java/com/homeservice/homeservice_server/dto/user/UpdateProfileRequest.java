package com.homeservice.homeservice_server.dto.user;

import com.homeservice.homeservice_server.validation.ValidationPatterns;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "กรุณากรอกชื่อ - นามสกุล")
    @Pattern(regexp = ValidationPatterns.FULL_NAME_PATTERN, message = "ชื่อ - นามสกุลต้องมีอักษรภาษาไทย หรืออักษรภาษาอังกฤษเท่านั้น")
    @Size(min = 2, message = "ชื่อ - นามสกุลต้องมีอย่างน้อย 2 ตัวอักษร")
    @Size(max = 100, message = "ชื่อ - นามสกุลต้องไม่เกิน 100 ตัวอักษร")
    private String fullname;

    @NotBlank(message = "กรุณากรอกเบอร์โทรศัพท์")
    @Pattern(regexp = ValidationPatterns.PHONE_PATTERN, message = "รูปแบบเบอร์โทรศัพท์ไม่ถูกต้อง")
    private String phone;
}
