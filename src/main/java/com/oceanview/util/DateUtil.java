package com.oceanview.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class DateUtil {
    private DateUtil() {
    }

    public static long calculateNights(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check-out dates are required.");
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }
        return nights;
    }
}
