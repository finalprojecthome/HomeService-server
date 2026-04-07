package com.homeservice.homeservice_server.repositories;

import com.homeservice.homeservice_server.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByOrderId(String orderId);
}