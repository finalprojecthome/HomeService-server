package com.homeservice.homeservice_server.services;

import com.homeservice.homeservice_server.dto.auth.GetUserResponse;
import com.homeservice.homeservice_server.dto.technician.TechnicianJobResponse;
import com.homeservice.homeservice_server.dto.technician.TechnicianProfileResponse;
import com.homeservice.homeservice_server.dto.technician.TechnicianSkillResponse;
import com.homeservice.homeservice_server.dto.technician.UpdateTechnicianProfileRequest;
import com.homeservice.homeservice_server.entities.Order;
import com.homeservice.homeservice_server.entities.Technician;
import com.homeservice.homeservice_server.entities.TechnicianService;
import com.homeservice.homeservice_server.entities.ServiceItem;
import com.homeservice.homeservice_server.entities.User;
import com.homeservice.homeservice_server.enums.ServiceStatus;
import com.homeservice.homeservice_server.enums.TechnicianJobStatus;
import com.homeservice.homeservice_server.exception.NotFoundException;
import com.homeservice.homeservice_server.repositories.OrderRepository;
import com.homeservice.homeservice_server.repositories.ServiceItemRepository;
import com.homeservice.homeservice_server.repositories.SubDistrictRepository;
import com.homeservice.homeservice_server.repositories.TechnicianRepository;
import com.homeservice.homeservice_server.repositories.TechnicianServiceRepository;
import com.homeservice.homeservice_server.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechnicianJobService {

    private final TechnicianRepository technicianRepository;
    private final TechnicianServiceRepository technicianServiceRepository;
    private final OrderRepository orderRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final SubDistrictRepository subDistrictRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    private Technician getTechnicianFromToken(String authorization) {
        String token = authorization.replaceFirst("(?i)^Bearer\\s+", "").trim();
        GetUserResponse userResponse = authService.getUser(token);
        return technicianRepository.findByUser_UserId(UUID.fromString(userResponse.getId()))
                .orElseThrow(() -> new NotFoundException("Technician not found for user: " + userResponse.getId()));
    }

    private TechnicianJobStatus mapToTechStatus(ServiceStatus status) {
        if (status == null) return null;
        return switch (status) {
            case PENDING -> TechnicianJobStatus.ASSIGNED;
            case IN_PROGRESS -> TechnicianJobStatus.IN_PROGRESS;
            case COMPLETED -> TechnicianJobStatus.COMPLETED;
        };
    }

    private TechnicianJobResponse mapToJobResponse(Order order) {
        BigDecimal totalPrice = order.getItems() != null ?
                order.getItems().stream()
                        .map(item -> item.getPricePerUnit().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;

        List<String> serviceItems = order.getItems() != null ?
                order.getItems().stream()
                        .map(item -> item.getServiceName())
                        .collect(Collectors.toList()) : Collections.emptyList();

        String customerName = "Unknown";
        if (order.getCustomerId() != null) {
            customerName = userRepository.findById(order.getCustomerId())
                    .map(User::getName)
                    .orElse("Unknown");
        }

        return TechnicianJobResponse.builder()
                .orderId(order.getOrderId())
                .status(mapToTechStatus(order.getStatus()))
                .customerName(customerName)
                .addressDetail(order.getAddressDetail())
                .scheduledAt(order.getScheduledAt())
                .totalPrice(totalPrice)
                .serviceItems(serviceItems)
                .build();
    }

    private double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }

        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double lonDistance = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public List<TechnicianJobResponse> getAvailableJobs(String authorization) {
        Technician technician = getTechnicianFromToken(authorization);

        // 1. Determine Technician's working center point
        BigDecimal techLat = technician.getLatitude();
        BigDecimal techLon = technician.getLongitude();

        // Fallback to SubDistrict coordinates if technician hasn't pinned their location
        if ((techLat == null || techLon == null) && technician.getSubDistrictId() != null) {
            subDistrictRepository.findById(technician.getSubDistrictId()).ifPresent(sd -> {
                technician.setLatitude(sd.getLatitude());
                technician.setLongitude(sd.getLongitude());
            });
            techLat = technician.getLatitude();
            techLon = technician.getLongitude();
        }

        if (techLat == null || techLon == null) {
            return Collections.emptyList();
        }

        final BigDecimal finalTechLat = techLat;
        final BigDecimal finalTechLon = techLon;

        // 2. Get Technician's skilled service names
        List<String> technicianSkillNames = technicianServiceRepository.findByTechnicianId(technician.getTechnicianId())
                .stream()
                .map(ts -> {
                    com.homeservice.homeservice_server.entities.ServiceItem svc = serviceItemRepository.findById(ts.getServiceId()).orElse(null);
                    return svc != null ? svc.getName() : null;
                })
                .filter(name -> name != null)
                .collect(Collectors.toList());

        // 3. Filter available jobs (PENDING, no tech assigned, within 7.5km, AND matches technician skills)
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == ServiceStatus.PENDING)
                .filter(o -> o.getTechnicianId() == null)
                .filter(o -> o.getLatitude() != null && o.getLongitude() != null)
                // Filter by skill: Order must have at least one item matching technician's skills
                .filter(o -> o.getItems().stream()
                        .anyMatch(item -> technicianSkillNames.contains(item.getServiceName())))
                // Filter by distance
                .filter(o -> calculateDistance(finalTechLat, finalTechLon, o.getLatitude(), o.getLongitude()) <= 7.5)
                .collect(Collectors.toList());

        return orders.stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void acceptJob(String orderId, String authorization) {
        Technician technician = getTechnicianFromToken(authorization);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (order.getTechnicianId() != null) {
            throw new IllegalStateException("Job already accepted by another technician.");
        }

        order.setTechnicianId(technician.getTechnicianId());
        order.setStatus(ServiceStatus.IN_PROGRESS); // Automatically move to in progress
        orderRepository.save(order);
    }

    public List<TechnicianJobResponse> getMyJobs(String authorization) {
        Technician technician = getTechnicianFromToken(authorization);

        List<Order> myOrders = orderRepository.findAll().stream()
                .filter(o -> technician.getTechnicianId().equals(o.getTechnicianId()))
                .collect(Collectors.toList());

        return myOrders.stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateJobStatus(String orderId, ServiceStatus status, String authorization) {
        Technician technician = getTechnicianFromToken(authorization);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (!technician.getTechnicianId().equals(order.getTechnicianId())) {
            throw new IllegalStateException("You are not assigned to this job.");
        }

        order.setStatus(status);
        orderRepository.save(order);
    }

    public TechnicianProfileResponse getProfile(String authorization) {
        Technician technician = getTechnicianFromToken(authorization);
        User user = technician.getUser();

        List<Integer> serviceIds = technicianServiceRepository.findByTechnicianId(technician.getTechnicianId())
                .stream()
                .map(TechnicianService::getServiceId)
                .collect(Collectors.toList());

        return TechnicianProfileResponse.builder()
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .addressDetail(technician.getAddressDetail())
                .subDistrictId(technician.getSubDistrictId())
                .latitude(technician.getLatitude())
                .longitude(technician.getLongitude())
                .bio(technician.getBio())
                .isAvailable(technician.getIsAvailable())
                .serviceIds(serviceIds)
                .build();
    }

    @Transactional
    public void updateProfile(UpdateTechnicianProfileRequest request, String authorization) {
        Technician technician = getTechnicianFromToken(authorization);
        User user = technician.getUser();

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        userRepository.save(user);

        technician.setIsAvailable(request.getIsAvailable());
        technician.setAddressDetail(request.getAddressDetail());
        technician.setSubDistrictId(request.getSubDistrictId());
        technician.setLatitude(request.getLatitude());
        technician.setLongitude(request.getLongitude());
        technician.setBio(request.getBio());
        technicianRepository.save(technician);

        technicianServiceRepository.deleteByTechnicianId(technician.getTechnicianId());
        if (request.getServiceIds() != null) {
            List<TechnicianService> skills = request.getServiceIds().stream()
                    .map(id -> TechnicianService.builder()
                            .technicianId(technician.getTechnicianId())
                            .serviceId(id)
                            .build())
                    .collect(Collectors.toList());
            technicianServiceRepository.saveAll(skills);
        }
    }

    public List<TechnicianSkillResponse> getAllServices() {
        return serviceItemRepository.findAll().stream()
                .map(s -> {
                    return TechnicianSkillResponse.builder()
                        .id(s.getServiceId())
                        .name(s.getName())
                        .build();
                })
                .collect(Collectors.toList());
    }
}
