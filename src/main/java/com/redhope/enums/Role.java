package com.redhope.enums;

/**
 * User roles in the system for RBAC.
 */
public enum Role {
    ROLE_SUPER_ADMIN("Super Administrator"),
    ROLE_HOSPITAL_ADMIN("Hospital Administrator"),
    ROLE_USER("Normal User");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
