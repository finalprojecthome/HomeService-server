package com.homeservice.homeservice_server.repository;

import com.homeservice.homeservice_server.entity.User;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
	Optional<User> findByEmailIgnoreCase(String email);

	@Modifying
	@Query(value = "update users set role = lower(role) where role is not null and role <> lower(role)", nativeQuery = true)
	int normalizeStoredRolesToLowercase();

	@Query(value = "select role from users where email = :email", nativeQuery = true)
	String findRawRoleByEmail(@Param("email") String email);
}
