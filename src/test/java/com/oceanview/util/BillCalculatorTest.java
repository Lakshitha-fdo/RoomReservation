package com.oceanview.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BillCalculatorTest {

    @Test
    void shouldCalculateBillTotal() {
        double total = BillCalculator.calculateTotal(2, 3500.0);
        assertEquals(7000.0, total);
    }

    @Test
    void shouldThrowForInvalidNights() {
        assertThrows(IllegalArgumentException.class, () -> BillCalculator.calculateTotal(0, 3500.0));
    }
}
