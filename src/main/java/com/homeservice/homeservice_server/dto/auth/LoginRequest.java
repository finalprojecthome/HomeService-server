package com.homeservice.homeservice_server.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "กรุณากรอกอีเมล")
    @Email(message = "รูปแบบอีเมลไม่ถูกต้อง")
    private String email;

    @NotBlank(message = "กรุณากรอกรหัสผ่าน")
    @Size(min = 12, message = "รหัสผ่านต้องมีอย่างน้อย 12 ตัวอักษร")
    private String password;
}
