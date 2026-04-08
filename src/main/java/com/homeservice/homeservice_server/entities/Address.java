package com.homeservice.homeservice_server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "addresses", uniqueConstraints = {
        @UniqueConstraint(name = "addresses_user_id_address_name_key", columnNames = { "user_id", "address_name" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id", nullable = false, updatable = false)
    private Integer addressId;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "uuid", foreignKey = @ForeignKey(name = "addresses_user_id_fkey"))
    private User user;

    @NotBlank
    @Size(max = 20)
    @Column(name = "address_name", nullable = false, length = 20)
    private String addressName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "address_detail", nullable = false, length = 100)
    private String addressDetail;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_district_id", nullable = false, foreignKey = @ForeignKey(name = "addresses_sub_district_id_fkey"))
    private SubDistrict subDistrict;

    @NotNull
    @DecimalMin(value = "-90", inclusive = true)
    @DecimalMax(value = "90", inclusive = true)
    @Column(name = "latitude", nullable = false, precision = 8, scale = 6)
    private BigDecimal latitude;

    @NotNull
    @DecimalMin(value = "-180", inclusive = true)
    @DecimalMax(value = "180", inclusive = true)
    @Column(name = "longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;
}
