package com.homeservice.homeservice_server.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {
	@Override
	public String convertToDatabaseColumn(UserRole attribute) {
		return attribute == null ? null : attribute.toExternalValue();
	}

	@Override
	public UserRole convertToEntityAttribute(String dbData) {
		return UserRole.fromExternalValue(dbData);
	}
}
