package com.oceanview.ui.controller;

import com.oceanview.client.ApiClient;
import com.oceanview.client.ClientResult;
import com.oceanview.model.Reservation;

public class ReservationController {
    private final ApiClient apiClient;

    public ReservationController(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ClientResult<Void> addReservation(Reservation reservation) {
        return apiClient.addReservation(reservation);
    }

    public ClientResult<Reservation> findReservation(String reservationId) {
        return apiClient.getReservation(reservationId);
    }
}
