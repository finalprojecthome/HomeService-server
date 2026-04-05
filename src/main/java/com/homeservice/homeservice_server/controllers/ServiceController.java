package com.homeservice.homeservice_server.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.homeservice.homeservice_server.dto.subservice.SubServiceResponse;
import com.homeservice.homeservice_server.services.SubServiceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ServiceController {

    private final SubServiceService subServiceService;

    // 🔥 GET sub-services by serviceId
    @GetMapping("/{serviceId}/sub-services")
    public List<SubServiceResponse> getSubServices(@PathVariable Long serviceId) {
        return subServiceService.getSubServicesByServiceId(serviceId);
    }
}