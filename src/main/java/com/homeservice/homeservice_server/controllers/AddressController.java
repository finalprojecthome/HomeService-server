package com.homeservice.homeservice_server.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.homeservice.homeservice_server.dto.address.DistrictResponse;
import com.homeservice.homeservice_server.dto.address.ProvinceResponse;
import com.homeservice.homeservice_server.dto.address.SubDistrictResponse;
import com.homeservice.homeservice_server.services.AddressService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/provinces")
    public ResponseEntity<List<ProvinceResponse>> getProvinces() {
        return ResponseEntity.ok(addressService.getProvinces());
    }

    @GetMapping("/districts/{provinceId}")
    public ResponseEntity<List<DistrictResponse>> getDistrictsByProvinceId(@PathVariable Integer provinceId) {
        return ResponseEntity.ok(addressService.getDistrictsByProvinceId(provinceId));
    }

    @GetMapping("/sub-districts/{districtId}")
    public ResponseEntity<List<SubDistrictResponse>> getSubDistrictsByDistrictId(@PathVariable Integer districtId) {
        return ResponseEntity.ok(addressService.getSubDistrictsByDistrictId(districtId));
    }

}
