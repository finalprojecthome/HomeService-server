package com.homeservice.homeservice_server.services;

import com.homeservice.homeservice_server.dto.order.CreateOrderRequest;
import com.homeservice.homeservice_server.entities.Order;
import com.homeservice.homeservice_server.entities.OrderItem;
import com.homeservice.homeservice_server.repositories.OrderRepository;
import com.homeservice.homeservice_server.repositories.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    @Transactional
    public String createOrder(CreateOrderRequest request) {
    
        Order order = Order.builder()
                .addressDetail(request.getAddressDetail())
                .subDistrictId(request.getSubDistrictId())
                .scheduledAt(request.getScheduledAt())
                .status("PENDING")
                .build();
    
        orderRepository.save(order);
        orderRepository.flush();
    
        String orderId = order.getOrderId();
    
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

    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}