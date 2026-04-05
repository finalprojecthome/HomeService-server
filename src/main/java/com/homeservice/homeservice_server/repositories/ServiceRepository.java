package com.homeservice.homeservice_server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.homeservice.homeservice_server.entities.Service;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    // ✅ หา service ตาม categoryId
    List<Service> findByCategory_CategoryId(Integer categoryId);

    // ✅ search จากชื่อ (case-insensitive)
    List<Service> findByNameContainingIgnoreCase(String name);

    // ✅ combine filter (category + search)
    List<Service> findByCategory_CategoryIdAndNameContainingIgnoreCase(
            Integer categoryId,
            String name
    );
}