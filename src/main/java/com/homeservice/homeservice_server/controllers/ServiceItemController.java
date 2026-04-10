package com.homeservice.homeservice_server.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.homeservice.homeservice_server.dto.service.ServiceItemResponse;
import com.homeservice.homeservice_server.dto.subservice.SubServiceResponse;
import com.homeservice.homeservice_server.security.CustomerOnly;
import com.homeservice.homeservice_server.services.ServiceItemService;
import com.homeservice.homeservice_server.services.SubServiceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceItemController {

    private final ServiceItemService serviceCatalogService;
    private final SubServiceService subServiceService;

    @GetMapping
    public List<ServiceItemResponse> getServices() {
        return serviceCatalogService.getServices();
    }

    @CustomerOnly
    @GetMapping("/{serviceId}/sub-services")
    public List<SubServiceResponse> getSubServices(@PathVariable Integer serviceId) {
        return subServiceService.getSubServicesByServiceId(serviceId);
    }
}