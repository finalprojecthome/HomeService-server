package com.homeservice.homeservice_server.dto.address;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ProvinceResponse {
    Integer id;
    String name;
}