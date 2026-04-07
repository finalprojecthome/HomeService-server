package com.homeservice.homeservice_server.dto.technician;

import com.homeservice.homeservice_server.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianJobDto {
    private String orderId;
    private OrderStatus status;
    private String customerName;
    private String addressDetail;
    private OffsetDateTime scheduledAt;
    private BigDecimal totalPrice;
    private List<String> serviceItems;
}
