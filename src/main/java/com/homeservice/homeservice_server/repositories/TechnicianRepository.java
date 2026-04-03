package com.homeservice.homeservice_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.homeservice.homeservice_server.entities.Technician;

public interface TechnicianRepository extends JpaRepository<Technician, Integer> {

}
