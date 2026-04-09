package com.homeservice.homeservice_server.controllers;

import com.homeservice.homeservice_server.entities.Order;
import com.homeservice.homeservice_server.security.RequestUserContext;
import com.homeservice.homeservice_server.security.CustomerOnly;
import com.homeservice.homeservice_server.dto.order.CreateOrderRequest;
import com.homeservice.homeservice_server.services.OrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @CustomerOnly
    @PostMapping
    public String createOrder(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CreateOrderRequest request) {
        UUID userId = (UUID) httpRequest.getAttribute(RequestUserContext.ATTR_USER_ID);
        return orderService.createOrder(request, userId);
    }

    @CustomerOnly
    @GetMapping("/my-orders")
    public List<Order> getMyOrders(HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute(RequestUserContext.ATTR_USER_ID);
        return orderService.getOrdersByCustomerId(userId);
    }
}