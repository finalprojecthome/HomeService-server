package com.homeservice.homeservice_server.repositories;

import com.homeservice.homeservice_server.entities.TechnicianService;
import com.homeservice.homeservice_server.entities.TechnicianServiceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicianServiceRepository extends JpaRepository<TechnicianService, TechnicianServiceId> {
    List<TechnicianService> findByTechnician_TechnicianId(Integer technicianId);
    void deleteByTechnician_TechnicianId(Integer technicianId);
}
