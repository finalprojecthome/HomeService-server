package com.homeservice.homeservice_server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.homeservice.homeservice_server.entities.District;

public interface DistrictRepository extends JpaRepository<District, Integer> {

    List<District> findByProvince_ProvinceId(Integer provinceId);
}
