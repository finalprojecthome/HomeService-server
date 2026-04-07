package com.homeservice.homeservice_server.controllers;

import org.springframework.http.ResponseEntity;
import com.homeservice.homeservice_server.entities.Order;
import com.homeservice.homeservice_server.dto.order.CreateOrderRequest;
import com.homeservice.homeservice_server.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public String createOrder(@RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }
}