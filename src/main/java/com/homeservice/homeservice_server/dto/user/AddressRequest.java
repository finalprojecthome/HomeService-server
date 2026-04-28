package com.homeservice.homeservice_server.dto.user;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class AddressRequest {
    @NotBlank(message = "กรุณากรอกชื่อของที่อยู่")
    @Size(max = 20, message = "ชื่อของที่อยู่ต้องไม่เกิน 20 ตัวอักษร")
    String addressName;

    @NotBlank(message = "กรุณากรอกที่อยู่")
    @Size(max = 100, message = "ที่อยู่ต้องไม่เกิน 100 ตัวอักษร")
    String addressDetail;

    @NotNull(message = "กรุณากรอกรหัสแขวง / ตำบล")
    Integer subDistrictId;

    @NotNull(message = "กรุณากรอกละติจูด")
    @DecimalMin(value = "-90", inclusive = true, message = "ละติจูดไม่ถูกต้อง")
    @DecimalMax(value = "90", inclusive = true, message = "ละติจูดไม่ถูกต้อง")
    BigDecimal latitude;

    @NotNull(message = "กรุณากรอกลองจิจูด")
    @DecimalMin(value = "-180", inclusive = true, message = "ลองจิจูดไม่ถูกต้อง")
    @DecimalMax(value = "180", inclusive = true, message = "ลองจิจูดไม่ถูกต้อง")
    BigDecimal longitude;
}
