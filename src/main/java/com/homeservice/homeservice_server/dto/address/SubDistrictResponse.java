package com.homeservice.homeservice_server.dto.address;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class SubDistrictResponse {
    Integer id;
    String name;
    BigDecimal latitude;
    BigDecimal longitude;
    Integer postCode;
}