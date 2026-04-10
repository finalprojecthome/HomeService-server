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
 * Isolated entity for technician skills.
 */
@Entity
@Table(name = "technician_skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TechnicianSkill.TechnicianSkillId.class)
public class TechnicianSkill {

    @Id
    @Column(name = "technician_id")
    private Integer technicianId;

    @Id
    @Column(name = "service_id")
    private Integer serviceId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnicianSkillId implements Serializable {
        private Integer technicianId;
        private Integer serviceId;
    }
}
