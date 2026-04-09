package com.homeservice.homeservice_server.persistence;

import com.homeservice.homeservice_server.enums.ServiceStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ServiceStatusConverter implements AttributeConverter<ServiceStatus, String> {

    @Override
    public String convertToDatabaseColumn(ServiceStatus attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public ServiceStatus convertToEntityAttribute(String dbData) {
        return ServiceStatus.fromDb(dbData);
    }
}
