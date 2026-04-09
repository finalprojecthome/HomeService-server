package com.homeservice.homeservice_server.services;

import com.homeservice.homeservice_server.dto.order.CreateOrderRequest;
import com.homeservice.homeservice_server.entities.Order;
import com.homeservice.homeservice_server.entities.OrderItem;
import com.homeservice.homeservice_server.enums.ServiceStatus;
import com.homeservice.homeservice_server.repositories.OrderRepository;
import com.homeservice.homeservice_server.repositories.OrderItemRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public String createOrder(CreateOrderRequest request, UUID customerId) {

        Order order = Order.builder()
                .customerId(customerId)
                .addressDetail(request.getAddressDetail())
                .subDistrictId(request.getSubDistrictId())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .scheduledAt(request.getScheduledAt())
                .status(ServiceStatus.PENDING)
                .build();

        Order saved = orderRepository.save(order);

        if (request.getItems() != null) {
            request.getItems().forEach(item -> {
                OrderItem orderItem = OrderItem.builder()
                        .order(saved)
                        .serviceName(item.getServiceName())
                        .quantity(item.getQuantity())
                        .pricePerUnit(BigDecimal.valueOf(item.getPricePerUnit()))
                        .build();

                orderItemRepository.save(orderItem);
            });
        }

        return saved.getOrderId();
    }

    public List<Order> getOrdersByCustomerId(UUID customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
}