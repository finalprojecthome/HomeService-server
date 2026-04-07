package com.homeservice.homeservice_server.dto.order;

import lombok.Data;

@Data
public class OrderItemRequest {
    private String serviceName;
    private Integer quantity;
    private Double pricePerUnit;
}