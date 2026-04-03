package com.homeservice.homeservice_server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "services")
public class ServiceItem {
	@Id
	@Column(name = "service_id", nullable = false)
	private Integer serviceId;

	@Column(name = "category_id", nullable = false)
	private Integer categoryId;
}
