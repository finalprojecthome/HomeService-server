package com.homeservice.homeservice_server.dto.order;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class CreateOrderRequest {
    private String addressDetail;
    private Integer subDistrictId;
    private OffsetDateTime scheduledAt;

    private List<OrderItemRequest> items;
}