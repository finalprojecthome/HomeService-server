package com.homeservice.homeservice_server.dto.user;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class GetAddressResponse {
    Integer id;
    String addressName;
    String addressDetail;
    AreaInfo province;
    AreaInfo district;
    AreaInfo subDistrict;
    Integer postCode;
    BigDecimal latitude;
    BigDecimal longitude;

    @Builder
    @Value
    public static class AreaInfo {
        Integer id;
        String name;
    }
}
