package com.homeservice.homeservice_server.repositories;

import com.homeservice.homeservice_server.entities.TechnicianJob;
import com.homeservice.homeservice_server.enums.TechnicianJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicianJobRepository extends JpaRepository<TechnicianJob, String> {
    List<TechnicianJob> findByTechnicianIdAndStatusIn(Integer technicianId, List<TechnicianJobStatus> statuses);
    Optional<TechnicianJob> findByOrderId(String orderId);
}
