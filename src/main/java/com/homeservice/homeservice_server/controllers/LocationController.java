package com.homeservice.homeservice_server.controllers;

import com.homeservice.homeservice_server.entities.District;
import com.homeservice.homeservice_server.entities.Province;
import com.homeservice.homeservice_server.entities.SubDistrict;
import com.homeservice.homeservice_server.repositories.DistrictRepository;
import com.homeservice.homeservice_server.repositories.ProvinceRepository;
import com.homeservice.homeservice_server.repositories.SubDistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final SubDistrictRepository subDistrictRepository;

    @GetMapping("/provinces")
    public List<Province> getAllProvinces() {
        return provinceRepository.findAll();
    }

    @GetMapping("/districts")
    public List<District> getDistricts(@RequestParam Integer provinceId) {
        return districtRepository.findByProvince_ProvinceId(provinceId);
    }

    @GetMapping("/sub-districts")
    public List<SubDistrict> getSubDistricts(@RequestParam Integer districtId) {
        return subDistrictRepository.findByDistrict_DistrictId(districtId);
    }
}
