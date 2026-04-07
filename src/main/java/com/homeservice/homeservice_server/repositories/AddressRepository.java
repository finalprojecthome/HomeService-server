package com.homeservice.homeservice_server.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.homeservice.homeservice_server.entities.Address;

public interface AddressRepository extends JpaRepository<Address, Integer> {

    @Query("""
            SELECT a FROM Address a
            JOIN FETCH a.subDistrict sd
            JOIN FETCH sd.district d
            JOIN FETCH d.province
            WHERE a.user.userId = :userId
            """)
    List<Address> findByUserIdWithLocation(@Param("userId") UUID userId);
}
