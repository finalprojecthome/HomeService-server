package com.homeservice.homeservice_server.controllers;

import com.homeservice.homeservice_server.dto.ServiceCatalogItemResponse;
import com.homeservice.homeservice_server.services.ServiceCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceCatalogController {
	private final ServiceCatalogService serviceCatalogService;

	public ServiceCatalogController(ServiceCatalogService serviceCatalogService) {
		this.serviceCatalogService = serviceCatalogService;
	}

	@GetMapping
	public List<ServiceCatalogItemResponse> list() {
		return serviceCatalogService.listServices();
	}
}
