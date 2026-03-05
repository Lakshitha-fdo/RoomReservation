package com.oceanview.model;

public class Bill {
    private final String reservationId;
    private final long nights;
    private final double nightlyRate;
    private final double total;

    public Bill(String reservationId, long nights, double nightlyRate, double total) {
        this.reservationId = reservationId;
        this.nights = nights;
        this.nightlyRate = nightlyRate;
        this.total = total;
    }

    public String getReservationId() {
        return reservationId;
    }

    public long getNights() {
        return nights;
    }

    public double getNightlyRate() {
        return nightlyRate;
    }

    public double getTotal() {
        return total;
    }
}
