package com.cinema.model;

/**
 * Enum representing different user roles in the system.
 */
public enum UserRole {
    REGULAR_USER("Regular User"),
    GUEST("Guest"),
    CASHIER("Cashier"),
    ADMIN("Admin");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
