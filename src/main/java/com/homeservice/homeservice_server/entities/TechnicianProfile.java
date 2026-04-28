package com.homeservice.homeservice_server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Isolated profile entity for technicians.
 * Stores extra data without modifying the base Technician or User entities.
 */
@Entity
@Table(name = "technician_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianProfile {
    @Id
    @Column(name = "technician_id", nullable = false)
    private Integer technicianId;

    @Column(name = "address_detail", columnDefinition = "text")
    private String addressDetail;

    @Column(name = "sub_district_id")
    private Integer subDistrictId;

    @Column(name = "bio", columnDefinition = "text")
    private String bio;
}
