package com.oceanview.ui.view;

import com.oceanview.client.ApiClient;
import com.oceanview.ui.controller.BillingController;
import com.oceanview.ui.controller.LoginController;
import com.oceanview.ui.controller.ReservationController;

import javax.swing.SwingUtilities;

public class ClientMain {
    public static void main(String[] args) {
        String baseUrl = System.getProperty("api.baseUrl", "http://localhost:8080");
        UiTheme.applyLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            ApiClient apiClient = new ApiClient(baseUrl);
            LoginController loginController = new LoginController(apiClient);
            ReservationController reservationController = new ReservationController(apiClient);
            BillingController billingController = new BillingController(apiClient);

            LoginView loginView = new LoginView(loginController, () -> {
                MainMenuView mainMenuView = new MainMenuView(reservationController, billingController);
                mainMenuView.setVisible(true);
            });
            loginView.setVisible(true);
        });
    }
}
