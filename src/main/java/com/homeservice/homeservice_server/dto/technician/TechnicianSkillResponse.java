package com.homeservice.homeservice_server.dto.technician;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianSkillResponse {
    private Integer id;
    private String name;
}
