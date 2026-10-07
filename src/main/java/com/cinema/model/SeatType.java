package com.cinema.model;

/**
 * Enum representing different types of seats.
 */
public enum SeatType {
    REGULAR("Regular", 1.0),
    VIP("VIP", 1.5),
    PREMIUM("Premium", 2.0);

    private final String displayName;
    private final double priceMultiplier;

    SeatType(String displayName, double priceMultiplier) {
        this.displayName = displayName;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }
}
