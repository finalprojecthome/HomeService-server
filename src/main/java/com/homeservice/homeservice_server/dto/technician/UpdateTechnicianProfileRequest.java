package com.homeservice.homeservice_server.dto.technician;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTechnicianProfileRequest {
    private String name;
    private String phone;
    private String email;
    private String addressDetail;
    private Integer subDistrictId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String bio;
    private Boolean isAvailable;
    private List<Integer> serviceIds;
}
