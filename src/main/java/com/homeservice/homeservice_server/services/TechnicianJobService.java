package com.homeservice.homeservice_server.services;

import com.homeservice.homeservice_server.dto.auth.GetUserResponse;
import com.homeservice.homeservice_server.dto.technician.TechnicianJobDto;
import com.homeservice.homeservice_server.entities.Order;
import com.homeservice.homeservice_server.entities.Technician;
import com.homeservice.homeservice_server.entities.TechnicianService;
import com.homeservice.homeservice_server.entities.User;
import com.homeservice.homeservice_server.enums.OrderStatus;
import com.homeservice.homeservice_server.exception.NotFoundException;
import com.homeservice.homeservice_server.repositories.OrderRepository;
import com.homeservice.homeservice_server.repositories.TechnicianRepository;
import com.homeservice.homeservice_server.repositories.TechnicianServiceRepository;
import com.homeservice.homeservice_server.repositories.UserRepository;
import com.homeservice.homeservice_server.repositories.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homeservice.homeservice_server.dto.technician.TechnicianProfileDto;
import com.homeservice.homeservice_server.dto.technician.UpdateProfileRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechnicianJobService {

    private final OrderRepository orderRepository;
    private final TechnicianRepository technicianRepository;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final TechnicianServiceRepository technicianServiceRepository;
    private final ServiceRepository serviceRepository;

    private Technician getTechnicianFromToken(String token) {
        GetUserResponse user = authService.getUser(token);
        return technicianRepository.findByUser_UserId(UUID.fromString(user.getId()))
                .orElseThrow(() -> new NotFoundException("Technician profile not found for user"));
    }

    private TechnicianJobDto mapToDto(Order order) {
        return TechnicianJobDto.builder()
                .orderId(order.getOrderId())
                .status(order.getStatus())
                .customerName(order.getCustomer() != null ? order.getCustomer().getName() : "Unknown")
                .addressDetail(order.getAddressDetail())
                .scheduledAt(order.getScheduledAt())
                .totalPrice(null) // Ideally calculated from OrderItems
                .serviceItems(Collections.emptyList()) // Ideally mapped from OrderItems
                .build();
    }

    public List<TechnicianJobDto> getAvailableJobs(String authorization) {
        String token = authorization.replaceFirst("(?i)^Bearer\\s+", "").trim();
        Technician technician = getTechnicianFromToken(token);

        if (technician.getUser().getSubDistrictId() == null) {
            return Collections.emptyList();
        }

        // Fetch pending orders in same sub-district
        List<Order> orders = orderRepository.findByStatusAndSubDistrict(
                OrderStatus.PENDING,
                technician.getUser().getSubDistrictId()
        );

        return orders.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void acceptJob(String orderId, String authorization) {
        String token = authorization.replaceFirst("(?i)^Bearer\\s+", "").trim();
        Technician technician = getTechnicianFromToken(token);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not available to accept. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.ACCEPTED); // or IN_PROGRESS depending on business flow
        order.setTechnician(technician);
        orderRepository.save(order);
    }

    public List<TechnicianJobDto> getMyJobs(String authorization) {
        String token = authorization.replaceFirst("(?i)^Bearer\\s+", "").trim();
        Technician technician = getTechnicianFromToken(token);

        List<Order> jobs = orderRepository.findByTechnicianUserIdAndStatusIn(
                UUID.fromString(technician.getUser().getUserId().toString()),
                List.of(OrderStatus.ACCEPTED, OrderStatus.IN_PROGRESS, OrderStatus.COMPLETED, OrderStatus.CANCELLED)
        );

        return jobs.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public void updateJobStatus(String orderId, OrderStatus newStatus, String authorization) {
        String token = authorization.replaceFirst("(?i)^Bearer\\s+", "").trim();
        Technician technician = getTechnicianFromToken(token);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (order.getTechnician() == null || !order.getTechnician().getTechnicianId().equals(technician.getTechnicianId())) {
            throw new IllegalStateException("You are not assigned to this job.");
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    public TechnicianProfileDto getProfile(String authorization) {
        String token = authorization.replaceFirst("(?i)^Bearer\\s+", "").trim();
        Technician technician = getTechnicianFromToken(token);
        User user = technician.getUser();

        List<Integer> serviceIds = technicianServiceRepository.findByTechnician_TechnicianId(technician.getTechnicianId())
                .stream()
                .map(ts -> ts.getService().getServiceId())
                .collect(Collectors.toList());

        return TechnicianProfileDto.builder()
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .addressDetail(user.getAddressDetail())
                .subDistrictId(user.getSubDistrictId())
                .isAvailable(technician.getIsAvailable())
                .serviceIds(serviceIds)
                .build();
    }

    @Transactional
    public void updateProfile(UpdateProfileRequest request, String authorization) {
        String token = authorization.replaceFirst("(?i)^Bearer\\s+", "").trim();
        Technician technician = getTechnicianFromToken(token);
        User user = technician.getUser();

        // Update User info
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setAddressDetail(request.getAddressDetail());
        user.setSubDistrictId(request.getSubDistrictId());
        userRepository.save(user);

        // Update Technician info
        technician.setIsAvailable(request.getIsAvailable());
        technicianRepository.save(technician);

// Update Services
        technicianServiceRepository.deleteByTechnician_TechnicianId(technician.getTechnicianId());
        
        if (request.getServiceIds() != null && !request.getServiceIds().isEmpty()) {
            List<TechnicianService> newServices = request.getServiceIds().stream().map(sId -> {
                com.homeservice.homeservice_server.entities.Service serviceObj = serviceRepository.findById(sId).orElse(null);
                if (serviceObj == null) return null;
                TechnicianService ts = new TechnicianService();
                ts.setTechnician(technician);
                ts.setService(serviceObj);
                return ts;
            }).filter(ts -> ts != null).collect(Collectors.toList());
            technicianServiceRepository.saveAll(newServices);
        }
    }

    public List<Map<String, Object>> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(s -> Map.<String, Object>of("id", s.getServiceId(), "title", s.getName()))
                .collect(Collectors.toList());
    }
}
