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

    // 🔥 map Entity → DTO (เหมือน AddressService)
    private SubServiceResponse toSubServiceResponse(SubService subService) {
        return SubServiceResponse.builder()
                .id(subService.getSubServiceId())
                .name(subService.getName())
                .unit(subService.getUnit())
                .pricePerUnit(subService.getPricePerUnit())
                .build();
    }

    // 🔥 main function
    public List<SubServiceResponse> getSubServicesByServiceId(Long serviceId) {

        // ✅ check ว่ามี service นี้จริงมั้ย
        if (!serviceRepository.existsById(serviceId)) {
            throw new NotFoundException("ไม่พบบริการ");
        }

        // ✅ query + map
        return subServiceRepository.findByService_ServiceId(serviceId)
                .stream()
                .map(this::toSubServiceResponse)
                .toList();
    }
}