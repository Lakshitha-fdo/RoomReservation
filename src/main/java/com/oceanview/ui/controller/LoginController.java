package com.oceanview.ui.controller;

import com.oceanview.client.ApiClient;
import com.oceanview.client.ClientResult;

public class LoginController {
    private final ApiClient apiClient;

    public LoginController(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ClientResult<Void> login(String username, String password) {
        return apiClient.login(username, password);
    }
}
