package com.homeservice.homeservice_server.enums;

/**
 * Isolated status for technician job assignments.
 * This is separate from the shared ServiceStatus to ensure zero intrusion.
 */
public enum TechnicianJobStatus {
    ASSIGNED("ได้รับมอบหมาย"),
    IN_PROGRESS("กำลังดำเนินการ"),
    COMPLETED("เสร็จสิ้น"),
    CANCELLED("ยกเลิก");

    private final String description;

    TechnicianJobStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
