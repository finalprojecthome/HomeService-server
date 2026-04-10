package com.homeservice.homeservice_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.homeservice.homeservice_server.entities.Technician;

import java.util.Optional;
import java.util.UUID;

public interface TechnicianRepository extends JpaRepository<Technician, Integer> {
    Optional<Technician> findByUser_UserId(UUID userId);
}
