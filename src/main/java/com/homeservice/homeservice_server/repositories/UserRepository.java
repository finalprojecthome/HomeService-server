package com.homeservice.homeservice_server.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.homeservice.homeservice_server.entities.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);
}
