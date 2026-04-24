package com.homeservice.homeservice_server.repositories;

import com.homeservice.homeservice_server.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByCustomerId(UUID customerId);
}