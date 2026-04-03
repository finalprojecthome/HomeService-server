package com.homeservice.homeservice_server.repository;

import com.homeservice.homeservice_server.entity.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Integer> {
	boolean existsByCategoryId(Integer categoryId);

	long countByCategoryId(Integer categoryId);

	void deleteAllByCategoryId(Integer categoryId);
}
