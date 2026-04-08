package com.homeservice.homeservice_server.dto.order;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderRequest {
    private UUID customerId;
    private String addressDetail;
    private Integer subDistrictId;
    private OffsetDateTime scheduledAt;

    private List<OrderItemRequest> items;
}