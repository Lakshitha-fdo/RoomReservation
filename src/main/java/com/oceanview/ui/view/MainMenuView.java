package com.oceanview.ui.view;

import com.oceanview.ui.controller.BillingController;
import com.oceanview.ui.controller.ReservationController;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.GridLayout;

public class MainMenuView extends JFrame {
    private final ReservationController reservationController;
    private final BillingController billingController;

    public MainMenuView(ReservationController reservationController, BillingController billingController) {
        this.reservationController = reservationController;
        this.billingController = billingController;

        setTitle("Ocean View Resort - Main Menu");
        setSize(420, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(6, 1, 8, 8));

        JButton addReservationButton = new JButton("1. Add Reservation");
        addReservationButton.addActionListener(e -> new ReservationFormDialog(this, reservationController).setVisible(true));
        panel.add(addReservationButton);

        JButton viewReservationButton = new JButton("2. View Reservation");
        viewReservationButton.addActionListener(e -> new ViewReservationDialog(this, reservationController).setVisible(true));
        panel.add(viewReservationButton);

        JButton billButton = new JButton("3. Calculate Bill");
        billButton.addActionListener(e -> new BillDialog(this, billingController).setVisible(true));
        panel.add(billButton);

        JButton helpButton = new JButton("4. Help");
        helpButton.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "1. Login using username and password\n"
                        + "2. Add reservation with required details\n"
                        + "3. View reservation by reservation number\n"
                        + "4. Calculate and print bill\n"
                        + "5. Exit safely from menu",
                "Help",
                JOptionPane.INFORMATION_MESSAGE));
        panel.add(helpButton);

        JButton exitButton = new JButton("5. Exit");
        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Do you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
            }
        });
        panel.add(exitButton);

        add(panel);
    }
}
