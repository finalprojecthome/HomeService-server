package com.homeservice.homeservice_server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sub_services")
public class SubServiceItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sub_service_id", nullable = false)
	private Integer subServiceId;

	@Column(name = "service_id", nullable = false)
	private Integer serviceId;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "unit", nullable = false)
	private String unit;

	@Column(name = "price_per_unit", nullable = false)
	private BigDecimal pricePerUnit;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@PrePersist
	void prePersist() {
		if (updatedAt == null) {
			updatedAt = OffsetDateTime.now();
		}
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = OffsetDateTime.now();
	}
}
