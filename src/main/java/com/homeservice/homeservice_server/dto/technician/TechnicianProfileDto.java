package com.homeservice.homeservice_server.dto.technician;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianProfileDto {
    private String name;
    private String phone;
    private String email;
    private String addressDetail;
    private Integer subDistrictId;
    private Boolean isAvailable;
    private List<Integer> serviceIds;
}
