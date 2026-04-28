package com.homeservice.homeservice_server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "sub_districts", indexes = @Index(name = "sub_districts_district_id_idx", columnList = "district_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubDistrict {

    @Id
    @Column(name = "sub_district_id", nullable = false)
    private Integer subDistrictId;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false, foreignKey = @ForeignKey(name = "sub_districts_district_id_fkey"))
    private District district;

    @NotBlank
    @Size(max = 120)
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @DecimalMin(value = "-90", inclusive = true)
    @DecimalMax(value = "90", inclusive = true)
    @Column(name = "latitude", precision = 5, scale = 3)
    private BigDecimal latitude;

    @DecimalMin(value = "-180", inclusive = true)
    @DecimalMax(value = "180", inclusive = true)
    @Column(name = "longitude", precision = 6, scale = 3)
    private BigDecimal longitude;

    @NotNull
    @Column(name = "post_code", nullable = false)
    private Integer postCode;
}