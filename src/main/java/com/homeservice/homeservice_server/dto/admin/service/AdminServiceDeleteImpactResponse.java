package com.homeservice.homeservice_server.dto.admin.service;

public record AdminServiceDeleteImpactResponse(
		Integer serviceId,
		boolean requiresForceDelete
) {
}
