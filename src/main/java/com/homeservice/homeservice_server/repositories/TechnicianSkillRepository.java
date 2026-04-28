package com.homeservice.homeservice_server.repositories;

import com.homeservice.homeservice_server.entities.TechnicianSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicianSkillRepository extends JpaRepository<TechnicianSkill, TechnicianSkill.TechnicianSkillId> {
    List<TechnicianSkill> findByTechnicianId(Integer technicianId);
    void deleteByTechnicianId(Integer technicianId);
}
