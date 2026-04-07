package com.homeservice.homeservice_server.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
    PENDING("pending"),
    ACCEPTED("accepted"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String dbValue;

    OrderStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    @JsonCreator
    public static OrderStatus fromString(String val) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.dbValue.equalsIgnoreCase(val)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus: " + val);
    }
}
