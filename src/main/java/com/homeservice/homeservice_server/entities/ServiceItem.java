package com.homeservice.homeservice_server.entities;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
		name = "services",
		indexes = @Index(name = "services_category_id_idx", columnList = "category_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "service_id", nullable = false)
	private Integer serviceId;

	@NotNull
	@Column(name = "category_id", nullable = false)
	private Integer categoryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "category_id",
			referencedColumnName = "category_id",
			foreignKey = @ForeignKey(name = "services_category_id_fkey"),
			insertable = false,
			updatable = false
	)
	private Category category;

	@NotBlank
	@Size(max = 255)
	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "image_url")
	private String imageUrl;

	@NotNull
	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@NotNull
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Column(name = "sort_order")
	private Integer sortOrder;

	public ServiceItem(Integer serviceId, Integer categoryId) {
		this.serviceId = serviceId;
		this.categoryId = categoryId;
	}

	@PrePersist
	void prePersist() {
		OffsetDateTime now = OffsetDateTime.now();
		if (createdAt == null) {
			createdAt = now;
		}
		if (updatedAt == null) {
			updatedAt = now;
		}
		if (sortOrder == null) {
			sortOrder = 0;
		}
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = OffsetDateTime.now();
	}
}
