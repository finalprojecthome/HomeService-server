package com.homeservice.homeservice_server.repositories;

import com.homeservice.homeservice_server.entities.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Integer> {
	boolean existsByCategoryId(Integer categoryId);

	long countByCategoryId(Integer categoryId);

	void deleteAllByCategoryId(Integer categoryId);

	@Query("SELECT DISTINCT s FROM ServiceItem s LEFT JOIN FETCH s.category LEFT JOIN FETCH s.subServices")
	List<ServiceItem> findAllWithCategoryAndSubServices();
}
