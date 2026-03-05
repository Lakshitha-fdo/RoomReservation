package com.oceanview.ui.controller;

import com.oceanview.client.ApiClient;
import com.oceanview.client.ClientResult;
import com.oceanview.model.Bill;

public class BillingController {
    private final ApiClient apiClient;

    public BillingController(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ClientResult<Bill> generateBill(String reservationId) {
        return apiClient.getBill(reservationId);
    }
}
