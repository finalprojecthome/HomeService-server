package com.homeservice.homeservice_server.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;






@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @Column(name = "order_id", updatable = false, nullable = false)
    private String orderId;

    @Column(name = "customer_id")
    private java.util.UUID customerId;

    @Column(name = "technician_id")
    private Integer technicianId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "address_detail", nullable = false, length = 120)
    private String addressDetail;

    @Column(name = "sub_district_id", nullable = false)
    private Integer subDistrictId;

    @Column(name = "scheduled_at", nullable = false)
    private OffsetDateTime scheduledAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;



    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<OrderItem> items;

    @PrePersist
    public void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}