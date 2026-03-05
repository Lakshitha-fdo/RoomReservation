package com.oceanview.util;

import com.oceanview.model.Reservation;
import com.oceanview.model.RoomType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationUtilTest {

    @Test
    void shouldReturnNoErrorsForValidReservation() {
        Reservation reservation = new Reservation("R001", "John Silva", "Galle", "0771234567", RoomType.DELUXE,
                LocalDate.of(2025, 11, 1), LocalDate.of(2025, 11, 3));

        List<String> errors = ValidationUtil.validateReservation(reservation);
        assertTrue(errors.isEmpty());
    }

    @Test
    void shouldReturnErrorsForInvalidReservation() {
        Reservation reservation = new Reservation("", "", "", "ABC", null,
                LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 1));

        List<String> errors = ValidationUtil.validateReservation(reservation);
        assertFalse(errors.isEmpty());
    }
}
