package com.oceanview.ui.controller;

import com.oceanview.client.ApiClient;
import com.oceanview.client.ClientResult;
import com.oceanview.model.Reservation;
import com.oceanview.model.ReservationPage;

public class ReservationController {
    private final ApiClient apiClient;

    public ReservationController(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ClientResult<Void> addReservation(Reservation reservation) {
        return apiClient.addReservation(reservation);
    }

    public ClientResult<String> getNextReservationId() {
        return apiClient.getNextReservationId();
    }

    public ClientResult<Reservation> findReservation(String reservationId) {
        return apiClient.getReservation(reservationId);
    }

    public ClientResult<ReservationPage> findReservations(String searchTerm, int page, int pageSize) {
        return apiClient.getReservations(searchTerm, page, pageSize);
    }

    public ClientResult<Void> updateReservation(Reservation reservation) {
        return apiClient.updateReservation(reservation);
    }
}
