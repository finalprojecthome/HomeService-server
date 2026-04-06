package com.homeservice.homeservice_server.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "กรุณากรอกรหัสผ่าน")
    @Size(min = 12, message = "รหัสผ่านเก่าต้องมีอย่างน้อย 12 ตัวอักษร")
    private String oldPassword;

    @NotBlank(message = "กรุณากรอกรหัสผ่าน")
    @Size(min = 12, message = "รหัสผ่านใหม่ต้องมีอย่างน้อย 12 ตัวอักษร")
    private String newPassword;
}
