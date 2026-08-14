package com.redhope.enums;

/**
 * Lifecycle status for blood donation appointments.
 */
public enum DonationStatus {
    PENDING("Pending Review"),
    APPROVED("Appointment Approved"),
    REJECTED("Appointment Rejected"),
    COMPLETED("Completed — Donation Recorded"),
    CANCELLED("Cancelled");

    private final String displayName;

    DonationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
