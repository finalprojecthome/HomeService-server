package com.homeservice.homeservice_server.repositories;

import com.homeservice.homeservice_server.entities.Order;
import com.homeservice.homeservice_server.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.subDistrict.subDistrictId = :subDistrictId")
    List<Order> findByStatusAndSubDistrict(
            @Param("status") OrderStatus status,
            @Param("subDistrictId") Integer subDistrictId);

    @Query("SELECT o FROM Order o WHERE o.technician.user.userId = :userId AND o.status IN :statuses")
    List<Order> findByTechnicianUserIdAndStatusIn(
            @Param("userId") UUID userId,
            @Param("statuses") List<OrderStatus> statuses);
}
