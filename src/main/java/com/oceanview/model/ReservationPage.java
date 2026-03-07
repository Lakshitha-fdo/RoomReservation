package com.oceanview.model;

import java.util.List;

public class ReservationPage {
    private final List<Reservation> reservations;
    private final int currentPage;
    private final int pageSize;
    private final int totalItems;

    public ReservationPage(List<Reservation> reservations, int currentPage, int pageSize, int totalItems) {
        this.reservations = reservations;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        if (pageSize <= 0) {
            return 1;
        }
        return Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
    }
}
