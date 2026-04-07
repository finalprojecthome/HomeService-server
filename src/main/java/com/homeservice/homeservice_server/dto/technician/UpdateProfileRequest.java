package com.homeservice.homeservice_server.dto.technician;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    
    @NotBlank
    private String name; // Frontend will concatenate Form's firstName and lastName into 'name', or we just accept 'name'

    @NotBlank
    @Pattern(regexp = "^0[1-9][0-9]{8}$", message = "Phone must be a valid Thai mobile number (10 digits starting with 0)")
    private String phone;

    // For location/address
    private String addressDetail;
    private Integer subDistrictId;

    @NotNull
    private Boolean isAvailable;

    // List of service IDs the technician is specialized in
    @NotNull
    private List<Integer> serviceIds;
}
