package com.homeservice.homeservice_server.repositories.admin;

import com.homeservice.homeservice_server.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminCategoryRepository extends JpaRepository<Category, Integer> {
	Page<Category> findByNameContainingIgnoreCase(String search, Pageable pageable);

	List<Category> findAllByOrderBySortOrderAscCategoryIdAsc();

	List<Category> findByNameContainingIgnoreCaseOrderBySortOrderAscCategoryIdAsc(String search);

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndCategoryIdNot(String name, Integer categoryId);

	Optional<Category> findTopByOrderBySortOrderDescCategoryIdDesc();
}
