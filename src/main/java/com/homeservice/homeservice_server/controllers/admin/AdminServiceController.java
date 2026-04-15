package com.homeservice.homeservice_server.controllers.admin;

import com.homeservice.homeservice_server.dto.admin.service.AdminServiceCreateRequest;
import com.homeservice.homeservice_server.dto.admin.service.AdminServiceDeleteImpactResponse;
import com.homeservice.homeservice_server.dto.admin.service.AdminServicePageResponse;
import com.homeservice.homeservice_server.dto.admin.service.AdminServicePatchRequest;
import com.homeservice.homeservice_server.dto.admin.service.AdminServiceReorderRequest;
import com.homeservice.homeservice_server.dto.admin.service.AdminServiceResponse;
import com.homeservice.homeservice_server.dto.admin.service.AdminServiceUpdateRequest;
import com.homeservice.homeservice_server.dto.admin.service.AdminUploadImageResponse;
import com.homeservice.homeservice_server.services.admin.AdminServiceService;
import com.homeservice.homeservice_server.services.admin.AdminUploadImageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/services")
public class AdminServiceController {
	private final AdminServiceService adminServiceService;
	private final AdminUploadImageService adminUploadImageService;

	public AdminServiceController(
			AdminServiceService adminServiceService,
			AdminUploadImageService adminUploadImageService
	) {
		this.adminServiceService = adminServiceService;
		this.adminUploadImageService = adminUploadImageService;
	}

	@PostMapping(path = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public AdminUploadImageResponse uploadImage(@RequestPart("image") MultipartFile image) {
		return adminUploadImageService.uploadServiceImage(image);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AdminServiceResponse create(@Valid @RequestBody AdminServiceCreateRequest request) {
		return adminServiceService.createService(request);
	}

	@GetMapping
	public AdminServicePageResponse list(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) Integer categoryId,
			@RequestParam(required = false) Integer page
	) {
		return adminServiceService.getServices(search, categoryId, page);
	}

	@GetMapping("/{serviceId}")
	public AdminServiceResponse detail(@PathVariable Integer serviceId) {
		return adminServiceService.getServiceById(serviceId);
	}

	@GetMapping("/{serviceId}/delete-impact")
	public AdminServiceDeleteImpactResponse deleteImpact(@PathVariable Integer serviceId) {
		return adminServiceService.getDeleteImpact(serviceId);
	}

	@PutMapping("/{serviceId}")
	public AdminServiceResponse update(
			@PathVariable Integer serviceId,
			@Valid @RequestBody AdminServiceUpdateRequest request
	) {
		return adminServiceService.updateService(serviceId, request);
	}

	@PatchMapping("/{serviceId}")
	public AdminServiceResponse patch(
			@PathVariable Integer serviceId,
			@Valid @RequestBody AdminServicePatchRequest request
	) {
		return adminServiceService.patchService(serviceId, request);
	}

	@DeleteMapping("/{serviceId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(
			@PathVariable Integer serviceId,
			@RequestParam(defaultValue = "false") boolean force
	) {
		adminServiceService.deleteService(serviceId, force);
	}

	@DeleteMapping("/{serviceId}/sub-services/{subServiceId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteSubService(
			@PathVariable Integer serviceId,
			@PathVariable Integer subServiceId
	) {
		adminServiceService.deleteSubService(serviceId, subServiceId);
	}

	@PutMapping("/reorder")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void reorder(@Valid @RequestBody AdminServiceReorderRequest request) {
		adminServiceService.reorderServices(
				request.scope(),
				request.serviceIds(),
				request.search(),
				request.categoryId(),
				request.page()
		);
	}
}
