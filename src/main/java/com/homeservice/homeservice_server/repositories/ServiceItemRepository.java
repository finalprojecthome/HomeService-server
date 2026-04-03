package com.homeservice.homeservice_server.repositories;

import com.homeservice.homeservice_server.entities.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Integer> {
	boolean existsByCategoryId(Integer categoryId);

	long countByCategoryId(Integer categoryId);

	void deleteAllByCategoryId(Integer categoryId);
}
