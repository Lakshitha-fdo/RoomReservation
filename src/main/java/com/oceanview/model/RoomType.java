package com.oceanview.model;

public enum RoomType {
    STANDARD(2000.0),
    DELUXE(3500.0),
    SUITE(4500.0);

    private final double nightlyRate;

    RoomType(double nightlyRate) {
        this.nightlyRate = nightlyRate;
    }

    public double nightlyRate() {
        return nightlyRate;
    }

    public static RoomType from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Room type is required.");
        }
        for (RoomType roomType : values()) {
            if (roomType.name().equalsIgnoreCase(value.trim())) {
                return roomType;
            }
        }
        throw new IllegalArgumentException("Unsupported room type: " + value);
    }
}
