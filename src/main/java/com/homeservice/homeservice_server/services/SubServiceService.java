package com.homeservice.homeservice_server.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.homeservice.homeservice_server.dto.subservice.SubServiceResponse;
import com.homeservice.homeservice_server.entities.SubService;
import com.homeservice.homeservice_server.exception.NotFoundException;
import com.homeservice.homeservice_server.repositories.ServiceRepository;
import com.homeservice.homeservice_server.repositories.SubServiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubServiceService {

    private final SubServiceRepository subServiceRepository;
    private final ServiceRepository serviceRepository;


    private SubServiceResponse toSubServiceResponse(SubService subService) {
        return SubServiceResponse.builder()
                .id(subService.getSubServiceId())
                .name(subService.getName())
                .unit(subService.getUnit())
                .pricePerUnit(subService.getPricePerUnit())
                .build();
    }


    public List<SubServiceResponse> getSubServicesByServiceId(Long serviceId) {


        if (!serviceRepository.existsById(serviceId)) {
            throw new NotFoundException("ไม่พบบริการ");
        }


        return subServiceRepository.findByService_ServiceId(serviceId)
                .stream()
                .map(this::toSubServiceResponse)
                .toList();
    }
}