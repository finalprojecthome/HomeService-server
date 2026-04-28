package com.homeservice.homeservice_server.repositories;

import com.homeservice.homeservice_server.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
}