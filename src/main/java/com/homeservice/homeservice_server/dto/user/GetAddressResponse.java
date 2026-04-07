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
    String province;
    String district;
    String subDistrict;
    Integer postCode;
    BigDecimal latitude;
    BigDecimal longitude;
}
