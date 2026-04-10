package com.homeservice.homeservice_server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Bridge entity for technicians and the services they are qualified to perform.
 * Matches the 'technicians_services' table in the ERD.
 */
@Entity
@Table(name = "technicians_services")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TechnicianService.TechnicianServiceId.class)
public class TechnicianService {

    @Id
    @Column(name = "technician_id", nullable = false)
    private Integer technicianId;

    @Id
    @Column(name = "service_id", nullable = false)
    private Integer serviceId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnicianServiceId implements Serializable {
        private Integer technicianId;
        private Integer serviceId;
    }
}
