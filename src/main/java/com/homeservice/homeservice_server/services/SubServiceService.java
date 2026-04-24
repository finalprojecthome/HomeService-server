package com.homeservice.homeservice_server.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.homeservice.homeservice_server.dto.subservice.SubServiceResponse;
import com.homeservice.homeservice_server.entities.SubService;
import com.homeservice.homeservice_server.exception.NotFoundException;
import com.homeservice.homeservice_server.repositories.ServiceItemRepository;
import com.homeservice.homeservice_server.repositories.SubServiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubServiceService {

    private final SubServiceRepository subServiceRepository;
    private final ServiceItemRepository serviceItemRepository;

    private SubServiceResponse toSubServiceResponse(SubService subService) {
        return SubServiceResponse.builder()
                .id(subService.getSubServiceId())
                .name(subService.getName())
                .unit(subService.getUnit())
                .pricePerUnit(subService.getPricePerUnit())
                .build();
    }

    public List<SubServiceResponse> getSubServicesByServiceId(Integer serviceId) {

        if (!serviceItemRepository.existsById(serviceId)) {
            throw new NotFoundException("ไม่พบบริการ");
        }

        return subServiceRepository.findByServiceItem_ServiceId(serviceId)
                .stream()
                .map(this::toSubServiceResponse)
                .toList();
    }
}