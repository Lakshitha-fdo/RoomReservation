package com.oceanview.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BillCalculatorTest {

    @Test
    void shouldCalculateBillTotal() {
        double total = BillCalculator.calculateTotal(2, 100.0);
        assertEquals(200.0, total);
    }

    @Test
    void shouldThrowForInvalidNights() {
        assertThrows(IllegalArgumentException.class, () -> BillCalculator.calculateTotal(0, 100.0));
    }
}
