package com.homeservice.homeservice_server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.homeservice.homeservice_server.entities.SubDistrict;

public interface SubDistrictRepository extends JpaRepository<SubDistrict, Integer> {

    List<SubDistrict> findByDistrict_DistrictId(Integer DistrictId);
}
