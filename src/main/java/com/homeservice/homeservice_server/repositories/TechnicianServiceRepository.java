package com.homeservice.homeservice_server.repositories;

import com.homeservice.homeservice_server.entities.TechnicianService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicianServiceRepository extends JpaRepository<TechnicianService, TechnicianService.TechnicianServiceId> {
    List<TechnicianService> findByTechnicianId(Integer technicianId);
    void deleteByTechnicianId(Integer technicianId);
}
