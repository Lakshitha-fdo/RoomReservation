package com.oceanview.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateUtilTest {

    @Test
    void shouldCalculateNightsCorrectly() {
        long nights = DateUtil.calculateNights(LocalDate.of(2025, 11, 1), LocalDate.of(2025, 11, 5));
        assertEquals(4, nights);
    }

    @Test
    void shouldThrowWhenCheckoutIsNotAfterCheckin() {
        assertThrows(IllegalArgumentException.class,
                () -> DateUtil.calculateNights(LocalDate.of(2025, 11, 5), LocalDate.of(2025, 11, 5)));
    }
}
