package com.homeservice.homeservice_server.dto.subservice;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class SubServiceResponse {
    Long id;
    String name;
    String unit;
    BigDecimal pricePerUnit;
}