package com.homeservice.homeservice_server.entity;

public enum UserRole {
	ADMIN;

	public String toExternalValue() {
		return name().toLowerCase();
	}

	public static UserRole fromExternalValue(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}

		return UserRole.valueOf(raw.trim().toUpperCase());
	}
}
