package com.redhope.enums;

/**
 * Urgency levels for blood requests.
 */
public enum Urgency {
    CRITICAL("Critical", "Immediate life risk — requires urgent action"),
    HIGH("High", "Urgent need within 24 hours"),
    NORMAL("Normal", "Standard request");

    private final String displayName;
    private final String description;

    Urgency(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
