package com.homeservice.homeservice_server.repositories.admin;

import com.homeservice.homeservice_server.entities.SubServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminSubServiceRepository extends JpaRepository<SubServiceItem, Integer> {
	List<SubServiceItem> findByServiceIdOrderBySubServiceIdAsc(Integer serviceId);

	List<SubServiceItem> findByServiceIdInOrderByServiceIdAscSubServiceIdAsc(List<Integer> serviceIds);

	void deleteAllByServiceId(Integer serviceId);

	long countByServiceId(Integer serviceId);
}
