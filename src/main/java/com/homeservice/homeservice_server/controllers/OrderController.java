package com.homeservice.homeservice_server.controllers;

import com.homeservice.homeservice_server.entities.Order;
import com.homeservice.homeservice_server.security.RequestUserContext;
import com.homeservice.homeservice_server.security.UserOnly;
import com.homeservice.homeservice_server.dto.order.CreateOrderRequest;
import com.homeservice.homeservice_server.services.OrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @UserOnly
    @PostMapping
    public String createOrder(
            @RequestBody CreateOrderRequest request,
            HttpServletRequest httpRequest
    ) {

        UUID userId = (UUID) httpRequest.getAttribute(RequestUserContext.ATTR_USER_ID);

        if (userId == null) {
            throw new RuntimeException("Unauthorized");
        }

        return orderService.createOrder(request, userId);
    }

    @UserOnly
    @GetMapping("/my-orders")
    public List<Order> getMyOrders(HttpServletRequest request) {
        return orderService.getOrdersByCustomerId(request);
    }
}