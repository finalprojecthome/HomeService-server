package com.homeservice.homeservice_server.repositories.admin;

import com.homeservice.homeservice_server.entities.ServiceItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdminServiceRepository extends JpaRepository<ServiceItem, Integer> {
	Page<ServiceItem> findAllByOrderBySortOrderAscServiceIdAsc(Pageable pageable);

	Page<ServiceItem> findByNameContainingIgnoreCaseOrderBySortOrderAscServiceIdAsc(String search, Pageable pageable);

	Page<ServiceItem> findByCategoryIdOrderBySortOrderAscServiceIdAsc(Integer categoryId, Pageable pageable);

	Page<ServiceItem> findByCategoryIdAndNameContainingIgnoreCaseOrderBySortOrderAscServiceIdAsc(
			Integer categoryId,
			String search,
			Pageable pageable
	);

	List<ServiceItem> findAllByOrderBySortOrderAscServiceIdAsc();

	List<ServiceItem> findByNameContainingIgnoreCaseOrderBySortOrderAscServiceIdAsc(String search);

	List<ServiceItem> findByCategoryIdOrderBySortOrderAscServiceIdAsc(Integer categoryId);

	List<ServiceItem> findByCategoryIdAndNameContainingIgnoreCaseOrderBySortOrderAscServiceIdAsc(
			Integer categoryId,
			String search
	);

	List<ServiceItem> findBySortOrderGreaterThanOrderBySortOrderAscServiceIdAsc(Integer sortOrder);

	@Modifying
	@Query("UPDATE ServiceItem s SET s.sortOrder = s.sortOrder - 1 WHERE s.sortOrder > :sortOrder")
	void decrementSortOrderGreaterThan(@Param("sortOrder") Integer sortOrder);

	boolean existsByCategoryIdAndNameIgnoreCase(Integer categoryId, String name);

	boolean existsByCategoryIdAndNameIgnoreCaseAndServiceIdNot(Integer categoryId, String name, Integer serviceId);

	Optional<ServiceItem> findTopByOrderBySortOrderDescServiceIdDesc();
}
