package com.homeservice.homeservice_server.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(
    name = "sub_services",
    indexes = @Index(name = "sub_services_service_id_idx", columnList = "service_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_service_id", nullable = false)
    private Long subServiceId;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "service_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "sub_services_service_id_fkey")
    )
    private Service service;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Size(max = 255)
    @Column(name = "unit", nullable = false)
    private String unit;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "price_per_unit", nullable = false, precision = 38, scale = 2)
    private BigDecimal pricePerUnit;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}