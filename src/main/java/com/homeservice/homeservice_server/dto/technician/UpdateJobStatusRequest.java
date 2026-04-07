package com.homeservice.homeservice_server.dto.technician;

import com.homeservice.homeservice_server.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateJobStatusRequest {
    @NotNull(message = "status is required")
    private OrderStatus status;
}
