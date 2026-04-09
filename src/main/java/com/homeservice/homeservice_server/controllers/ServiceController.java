package com.homeservice.homeservice_server.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.homeservice.homeservice_server.dto.ServiceCatalogItemResponse;
import com.homeservice.homeservice_server.dto.subservice.SubServiceResponse;
import com.homeservice.homeservice_server.security.UserOnly;
import com.homeservice.homeservice_server.services.ServiceCatalogService;
import com.homeservice.homeservice_server.services.SubServiceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceCatalogService serviceCatalogService;
    private final SubServiceService subServiceService;

    @GetMapping
    public List<ServiceCatalogItemResponse> getServices() {
        return serviceCatalogService.getServices();
    }

    @UserOnly
    @GetMapping("/{serviceId}/sub-services")
    public List<SubServiceResponse> getSubServices(@PathVariable Long serviceId) {
        return subServiceService.getSubServicesByServiceId(serviceId);
    }
}