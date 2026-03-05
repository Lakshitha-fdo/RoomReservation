package com.oceanview.util;

import com.oceanview.model.Reservation;
import com.oceanview.model.RoomType;

import java.util.ArrayList;
import java.util.List;

public final class ValidationUtil {
    private ValidationUtil() {
    }

    public static List<String> validateReservation(Reservation reservation) {
        List<String> errors = new ArrayList<>();
        if (reservation == null) {
            errors.add("Reservation payload is required.");
            return errors;
        }

        if (isBlank(reservation.getReservationId())) {
            errors.add("Reservation number is required.");
        }
        if (isBlank(reservation.getGuestName())) {
            errors.add("Guest name is required.");
        }
        if (isBlank(reservation.getAddress())) {
            errors.add("Address is required.");
        }
        if (isBlank(reservation.getContactNumber())) {
            errors.add("Contact number is required.");
        } else if (!reservation.getContactNumber().matches("\\d+")) {
            errors.add("Contact number must be numeric.");
        }

        RoomType roomType = reservation.getRoomType();
        if (roomType == null) {
            errors.add("Room type is required.");
        }

        try {
            DateUtil.calculateNights(reservation.getCheckInDate(), reservation.getCheckOutDate());
        } catch (IllegalArgumentException e) {
            errors.add(e.getMessage());
        }

        return errors;
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
