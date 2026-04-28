package com.homeservice.homeservice_server.dto.order;

import com.homeservice.homeservice_server.enums.ServiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {
    private ServiceStatus status;
}
