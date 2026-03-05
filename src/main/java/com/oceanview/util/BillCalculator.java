package com.oceanview.util;

public final class BillCalculator {
    private BillCalculator() {
    }

    public static double calculateTotal(long nights, double nightlyRate) {
        if (nights <= 0) {
            throw new IllegalArgumentException("Nights must be greater than zero.");
        }
        if (nightlyRate < 0) {
            throw new IllegalArgumentException("Rate cannot be negative.");
        }
        return nights * nightlyRate;
    }
}
