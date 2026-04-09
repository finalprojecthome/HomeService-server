package com.homeservice.homeservice_server.dto.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Data
public class CreateOrderRequest {

    @NotBlank(message = "กรุณากรอกที่อยู่")
    @Size(max = 100, message = "ที่อยู่ต้องไม่เกิน 100 ตัวอักษร")
    private String addressDetail;

    @NotNull(message = "กรุณากรอกรหัสแขวง / ตำบล")
    @Positive(message = "รหัสแขวง / ตำบลต้องเป็นจำนวนเต็มบวก")
    private Integer subDistrictId;

    @NotNull(message = "กรุณากรอกละติจูด")
    @DecimalMin(value = "-90", inclusive = true, message = "ละติจูดไม่ถูกต้อง")
    @DecimalMax(value = "90", inclusive = true, message = "ละติจูดไม่ถูกต้อง")
    private BigDecimal latitude;

    @NotNull(message = "กรุณากรอกลองจิจูด")
    @DecimalMin(value = "-180", inclusive = true, message = "ลองจิจูดไม่ถูกต้อง")
    @DecimalMax(value = "180", inclusive = true, message = "ลองจิจูดไม่ถูกต้อง")
    private BigDecimal longitude;

    private OffsetDateTime scheduledAt;

    @Valid
    private List<OrderItemRequest> items;
}