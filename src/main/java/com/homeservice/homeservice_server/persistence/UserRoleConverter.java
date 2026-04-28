package com.homeservice.homeservice_server.persistence;

import com.homeservice.homeservice_server.enums.UserRole;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        return UserRole.fromDb(dbData);
    }
}
