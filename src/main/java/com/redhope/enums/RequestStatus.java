package com.redhope.enums;

/**
 * Lifecycle status for blood requests.
 */
public enum RequestStatus {
    PENDING("Pending Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    COMPLETED("Completed — Blood Collected"),
    CANCELLED("Cancelled");

    private final String displayName;

    RequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
