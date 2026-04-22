package com.homeservice.homeservice_server.repositories;

import com.homeservice.homeservice_server.entities.TechnicianProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TechnicianProfileRepository extends JpaRepository<TechnicianProfile, Integer> {
}
