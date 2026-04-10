package com.homeservice.homeservice_server.entities;

import com.homeservice.homeservice_server.enums.TechnicianJobStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Isolated entity to track technician job assignments.
 * Links to the core Order table via orderId (String) without JPA relations
 * to ensure 100% isolation for teammates.
 */
@Entity
@Table(name = "technician_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianJob {

    @Id
    @Column(name = "order_id", nullable = false, length = 16)
    private String orderId;

    @Column(name = "technician_id", nullable = false)
    private Integer technicianId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TechnicianJobStatus status;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        assignedAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
