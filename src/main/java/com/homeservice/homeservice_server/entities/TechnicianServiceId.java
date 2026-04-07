package com.homeservice.homeservice_server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TechnicianServiceId implements Serializable {

    @Column(name = "technician_id")
    private Integer technicianId;

    @Column(name = "service_id")
    private Integer serviceId;
}
