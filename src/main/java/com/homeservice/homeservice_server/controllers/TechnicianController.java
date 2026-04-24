package com.homeservice.homeservice_server.controllers;

import com.homeservice.homeservice_server.dto.technician.TechnicianJobResponse;
import com.homeservice.homeservice_server.dto.technician.TechnicianProfileResponse;
import com.homeservice.homeservice_server.dto.technician.TechnicianSkillResponse;
import com.homeservice.homeservice_server.dto.technician.UpdateTechnicianProfileRequest;
import com.homeservice.homeservice_server.enums.ServiceStatus;
import com.homeservice.homeservice_server.services.TechnicianJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/technician")
@RequiredArgsConstructor
public class TechnicianController {

    private final TechnicianJobService technicianJobService;

    @GetMapping("/available-jobs")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<List<TechnicianJobResponse>> getAvailableJobs(
            @RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(technicianJobService.getAvailableJobs(authorization));
    }

    @PostMapping("/accept-job/{orderId}")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<Void> acceptJob(
            @PathVariable String orderId,
            @RequestHeader("Authorization") String authorization) {
        technicianJobService.acceptJob(orderId, authorization);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-jobs")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<List<TechnicianJobResponse>> getMyJobs(
            @RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(technicianJobService.getMyJobs(authorization));
    }

    @PatchMapping("/update-job-status/{orderId}")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<Void> updateJobStatus(
            @PathVariable String orderId,
            @RequestBody Map<String, ServiceStatus> statusMap,
            @RequestHeader("Authorization") String authorization) {
        technicianJobService.updateJobStatus(orderId, statusMap.get("status"), authorization);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<TechnicianProfileResponse> getProfile(
            @RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(technicianJobService.getProfile(authorization));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<Void> updateProfile(
            @RequestBody UpdateTechnicianProfileRequest request,
            @RequestHeader("Authorization") String authorization) {
        technicianJobService.updateProfile(request, authorization);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/services")
    public ResponseEntity<List<TechnicianSkillResponse>> getServices() {
        return ResponseEntity.ok(technicianJobService.getAllServices());
    }
}
