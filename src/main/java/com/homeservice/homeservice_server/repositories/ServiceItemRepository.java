package com.homeservice.homeservice_server.repositories;

import com.homeservice.homeservice_server.entities.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Integer> {

	boolean existsByCategory_CategoryId(Integer categoryId);

	long countByCategory_CategoryId(Integer categoryId);

	void deleteAllByCategory_CategoryId(Integer categoryId);

	// หา service ตาม categoryId
	List<ServiceItem> findByCategory_CategoryId(Integer categoryId);

	// search จากชื่อ (case-insensitive)
	List<ServiceItem> findByNameContainingIgnoreCase(String name);

	// combine filter (category + search)
	List<ServiceItem> findByCategory_CategoryIdAndNameContainingIgnoreCase(
			Integer categoryId,
			String name);

	@Query("SELECT DISTINCT s FROM ServiceItem s LEFT JOIN FETCH s.category")
	List<ServiceItem> findAllWithCategoryAndSubServices();
}
