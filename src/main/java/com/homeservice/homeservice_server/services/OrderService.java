package com.homeservice.homeservice_server.services;

import com.homeservice.homeservice_server.dto.order.CreateOrderRequest;
import com.homeservice.homeservice_server.entities.Order;
import com.homeservice.homeservice_server.entities.OrderItem;
import com.homeservice.homeservice_server.repositories.OrderRepository;
import com.homeservice.homeservice_server.repositories.OrderItemRepository;
import com.homeservice.homeservice_server.security.RequestUserContext;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
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

        String orderId = "AD" + System.currentTimeMillis();

        Order order = Order.builder()
                .orderId(orderId)
                .customerId(customerId) // 👈 ใช้ param ตรง ๆ
                .addressDetail(request.getAddressDetail())
                .subDistrictId(request.getSubDistrictId())
                .scheduledAt(request.getScheduledAt())
                .status("PENDING")
                .build();

        orderRepository.save(order);

        if (request.getItems() != null) {
            request.getItems().forEach(item -> {
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .serviceName(item.getServiceName())
                        .quantity(item.getQuantity())
                        .pricePerUnit(BigDecimal.valueOf(item.getPricePerUnit()))
                        .build();

                orderItemRepository.save(orderItem);
            });
        }

        return orderId;
    }

    public List<Order> getOrdersByCustomerId(HttpServletRequest request) {

        UUID customerId = (UUID) request.getAttribute(RequestUserContext.ATTR_USER_ID);

        if (customerId == null) {
            throw new RuntimeException("Unauthorized");
        }

        return orderRepository.findByCustomerId(customerId);
    }
}