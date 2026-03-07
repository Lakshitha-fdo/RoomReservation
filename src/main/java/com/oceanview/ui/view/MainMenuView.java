package com.oceanview.ui.view;

import com.oceanview.ui.controller.BillingController;
import com.oceanview.ui.controller.ReservationController;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

public class MainMenuView extends JFrame {
    private final ReservationController reservationController;
    private final BillingController billingController;

    public MainMenuView(ReservationController reservationController, BillingController billingController) {
        this.reservationController = reservationController;
        this.billingController = billingController;

        setTitle("Ocean View Resort - Main Menu");
        setSize(UiTheme.WINDOW_WIDTH, UiTheme.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel container = new JPanel(new BorderLayout(12, 12));
        UiTheme.addPanelPadding(container);

        JLabel title = new JLabel("Reservation Management", SwingConstants.CENTER);
        title.setFont(UiTheme.TITLE_FONT);
        container.add(title, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        JButton addReservationButton = new JButton("1. Add Reservation");
        UiTheme.styleButton(addReservationButton);
        addReservationButton.addActionListener(e -> openDialog(new ReservationFormDialog(this, reservationController)));
        panel.add(addReservationButton);

        JButton viewReservationButton = new JButton("2. Manage Reservations");
        UiTheme.styleButton(viewReservationButton);
        viewReservationButton.addActionListener(e -> openDialog(new ViewReservationDialog(this, reservationController)));
        panel.add(viewReservationButton);

        JButton billButton = new JButton("3. Print Bill");
        UiTheme.styleButton(billButton);
        billButton.addActionListener(e -> openDialog(new BillDialog(this, billingController)));
        panel.add(billButton);

        JButton helpButton = new JButton("4. Help");
        UiTheme.styleButton(helpButton);
        helpButton.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "1. Login using username and password\n"
                        + "2. Add reservation with auto-generated reservation number\n"
                        + "3. Manage reservations using search, pagination, and inline updates\n"
                        + "4. Generate and print a formatted bill\n"
                        + "5. Exit safely from menu",
                "Help",
                JOptionPane.INFORMATION_MESSAGE));
        panel.add(helpButton);

        JButton exitButton = new JButton("5. Exit");
        UiTheme.styleButton(exitButton);
        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Do you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
            }
        });
        panel.add(exitButton);

        for (Component component : panel.getComponents()) {
            if (component instanceof JButton button) {
                button.setAlignmentX(Component.CENTER_ALIGNMENT);
            }
        }

        container.add(panel, BorderLayout.CENTER);
        add(container);
    }

    private void openDialog(JDialog dialog) {
        setVisible(false);
        try {
            dialog.setVisible(true);
        } finally {
            setVisible(true);
            toFront();
        }
    }
}
