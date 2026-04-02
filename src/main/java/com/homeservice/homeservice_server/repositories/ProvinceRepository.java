package com.homeservice.homeservice_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.homeservice.homeservice_server.entities.Province;

public interface ProvinceRepository extends JpaRepository<Province, Integer> {

}
