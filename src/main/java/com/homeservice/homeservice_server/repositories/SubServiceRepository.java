package com.homeservice.homeservice_server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.homeservice.homeservice_server.entities.SubService;

public interface SubServiceRepository extends JpaRepository<SubService, Integer> {

    List<SubService> findByServiceItem_ServiceId(Integer serviceId);
}