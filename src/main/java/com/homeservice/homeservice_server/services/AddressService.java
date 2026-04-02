package com.homeservice.homeservice_server.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.homeservice.homeservice_server.dto.address.DistrictResponse;
import com.homeservice.homeservice_server.dto.address.ProvinceResponse;
import com.homeservice.homeservice_server.dto.address.SubDistrictResponse;
import com.homeservice.homeservice_server.entities.District;
import com.homeservice.homeservice_server.entities.Province;
import com.homeservice.homeservice_server.entities.SubDistrict;
import com.homeservice.homeservice_server.exception.NotFoundException;
import com.homeservice.homeservice_server.repositories.DistrictRepository;
import com.homeservice.homeservice_server.repositories.ProvinceRepository;
import com.homeservice.homeservice_server.repositories.SubDistrictRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final SubDistrictRepository subDistrictRepository;

    private ProvinceResponse toProvinceResponse(Province province) {
        return ProvinceResponse.builder()
                .id(province.getProvinceId())
                .name(province.getName())
                .build();
    }

    private DistrictResponse toDistrictResponse(District district) {
        return DistrictResponse.builder()
                .id(district.getDistrictId())
                .name(district.getName())
                .build();
    }

    private SubDistrictResponse toSubDistrict(SubDistrict subDistrict) {
        return SubDistrictResponse.builder()
                .id(subDistrict.getSubDistrictId())
                .name(subDistrict.getName())
                .latitude(subDistrict.getLatitude())
                .longitude(subDistrict.getLongitude())
                .postCode(subDistrict.getPostCode())
                .build();
    }

    public List<ProvinceResponse> getProvince() {
        return provinceRepository.findAll()
                .stream()
                .map(this::toProvinceResponse)
                .toList();
    }

    public List<DistrictResponse> getDistrictByProvinceId(Integer provinceId) {
        if (!provinceRepository.existsById(provinceId)) {
            throw new NotFoundException("ไม่พบจังหวัด");
        }
        return districtRepository.findByProvince_ProvinceId(provinceId)
                .stream()
                .map(this::toDistrictResponse)
                .toList();
    }

    public List<SubDistrictResponse> getSubDistrictByDistrictId(Integer districtId) {
        if (!districtRepository.existsById(districtId)) {
            if (districtId >= 1100) {
                throw new NotFoundException("ไม่พบอำเภอ");
            }
            throw new NotFoundException("ไม่พบเขต");
        }
        return subDistrictRepository.findByDistrict_DistrictId(districtId)
                .stream()
                .map(this::toSubDistrict)
                .toList();
    }
}
