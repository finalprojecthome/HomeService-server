package com.homeservice.homeservice_server.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Maps to PostgreSQL {@code service_status} enum labels.
 */
public enum ServiceStatus {
    PENDING("รอดำเนินการ"),
    IN_PROGRESS("กำลังดำเนินการ"),
    COMPLETED("ดำเนินการสำเร็จ");

    private final String dbValue;

    ServiceStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    public static ServiceStatus tryFromDb(String value) {
        if (value == null) {
            return null;
        }
        for (ServiceStatus s : values()) {
            if (s.dbValue.equals(value)) {
                return s;
            }
        }
        return null;
    }

    @JsonCreator
    public static ServiceStatus fromDb(String value) {
        ServiceStatus resolved = tryFromDb(value);
        if (value != null && resolved == null) {
            throw new IllegalArgumentException("Unknown service_status: " + value);
        }
        return resolved;
    }
}
