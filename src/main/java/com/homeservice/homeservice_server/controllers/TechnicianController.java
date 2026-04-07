package com.homeservice.homeservice_server.controllers;

import com.homeservice.homeservice_server.dto.technician.TechnicianJobDto;
import com.homeservice.homeservice_server.dto.technician.TechnicianProfileDto;
import com.homeservice.homeservice_server.dto.technician.UpdateJobStatusRequest;
import com.homeservice.homeservice_server.dto.technician.UpdateProfileRequest;
import com.homeservice.homeservice_server.services.TechnicianJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/technician")
@RequiredArgsConstructor
public class TechnicianController {

    private final TechnicianJobService technicianJobService;

    @GetMapping("/jobs/available")
    public ResponseEntity<List<TechnicianJobDto>> getAvailableJobs(
            @RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(technicianJobService.getAvailableJobs(authorization));
    }

    @PostMapping("/jobs/{orderId}/accept")
    public ResponseEntity<Map<String, String>> acceptJob(
            @PathVariable String orderId,
            @RequestHeader("Authorization") String authorization) {
        technicianJobService.acceptJob(orderId, authorization);
        return ResponseEntity.ok(Map.of("message", "Job accepted successfully"));
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<TechnicianJobDto>> getMyJobs(
            @RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(technicianJobService.getMyJobs(authorization));
    }

    @PutMapping("/jobs/{orderId}/status")
    public ResponseEntity<Map<String, String>> updateJobStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateJobStatusRequest request,
            @RequestHeader("Authorization") String authorization) {
        technicianJobService.updateJobStatus(orderId, request.getStatus(), authorization);
        return ResponseEntity.ok(Map.of("message", "Job status updated successfully"));
    }

    @GetMapping("/profile")
    public ResponseEntity<TechnicianProfileDto> getProfile(
            @RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(technicianJobService.getProfile(authorization));
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, String>> updateProfile(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody UpdateProfileRequest request) {
        technicianJobService.updateProfile(request, authorization);
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    @GetMapping("/services")
    public ResponseEntity<List<Map<String, Object>>> getServices() {
        return ResponseEntity.ok(technicianJobService.getAllServices());
    }
}
