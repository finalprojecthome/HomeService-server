package com.homeservice.homeservice_server.dto.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrderItemRequest {

    @NotNull(message = "กรุณากรอกชื่อบริการ")
    @Size(max = 255, message = "ชื่อบริการต้องไม่เกิน 255 ตัวอักษร")
    private String serviceName;

    @NotNull(message = "กรุณากรอกจำนวน")
    @Positive(message = "จำนวนต้องมากกว่า 0")
    private Integer quantity;

    @NotNull(message = "กรุณากรอกราคาต่อหน่วย")
    @DecimalMin(value = "0", inclusive = true, message = "ราคาต่อหน่วยต้องมากกว่าหรือเท่ากับ 0")
    private Double pricePerUnit;
}