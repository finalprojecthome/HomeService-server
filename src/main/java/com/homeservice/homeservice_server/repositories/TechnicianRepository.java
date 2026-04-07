package com.homeservice.homeservice_server.repositories;

import com.homeservice.homeservice_server.entities.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Integer> {
    Optional<Technician> findByUser_UserId(UUID userId);
}
