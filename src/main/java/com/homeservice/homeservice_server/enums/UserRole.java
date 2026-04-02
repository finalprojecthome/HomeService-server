package com.homeservice.homeservice_server.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
    USER("user"),
    TECHNICIAN("technician"),
    ADMIN("admin");

    private final String dbValue;

    UserRole(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    /**
     * Resolves API/DB string to enum, or null if no match (e.g. invalid JSON
     * input).
     */
    public static UserRole tryFromDb(String value) {
        if (value == null) {
            return null;
        }
        for (UserRole r : values()) {
            if (r.dbValue.equals(value)) {
                return r;
            }
        }
        return null;
    }

    /**
     * JPA and Jackson: unknown non-null values throw (invalid DB row or strict JSON
     * enum).
     */
    @JsonCreator
    public static UserRole fromDb(String value) {
        UserRole resolved = tryFromDb(value);
        if (value != null && resolved == null) {
            throw new IllegalArgumentException("Unknown user_role: " + value);
        }
        return resolved;
    }
}
