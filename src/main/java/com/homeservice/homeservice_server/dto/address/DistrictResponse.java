package com.homeservice.homeservice_server.dto.address;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class DistrictResponse {
    Integer id;
    String name;
}