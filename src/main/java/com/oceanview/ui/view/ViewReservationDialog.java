package com.oceanview.ui.view;

import com.oceanview.client.ClientResult;
import com.oceanview.model.Reservation;
import com.oceanview.ui.controller.ReservationController;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;

public class ViewReservationDialog extends JDialog {
    public ViewReservationDialog(JFrame parent, ReservationController reservationController) {
        super(parent, "View Reservation", true);
        setSize(500, 320);
        setLocationRelativeTo(parent);

        JTextField reservationIdField = new JTextField();
        JButton searchButton = new JButton("Search");
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(new JLabel("Reservation Number:"), BorderLayout.WEST);
        top.add(reservationIdField, BorderLayout.CENTER);
        top.add(searchButton, BorderLayout.EAST);

        searchButton.addActionListener(e -> {
            ClientResult<Reservation> result = reservationController.findReservation(reservationIdField.getText().trim());
            if (!result.success()) {
                JOptionPane.showMessageDialog(this, result.message(), "Not Found", JOptionPane.ERROR_MESSAGE);
                resultArea.setText("");
                return;
            }

            Reservation reservation = result.data();
            String details = "Reservation No: " + reservation.getReservationId() + "\n"
                    + "Guest Name: " + reservation.getGuestName() + "\n"
                    + "Address: " + reservation.getAddress() + "\n"
                    + "Contact Number: " + reservation.getContactNumber() + "\n"
                    + "Room Type: " + reservation.getRoomType() + "\n"
                    + "Check-in: " + reservation.getCheckInDate() + "\n"
                    + "Check-out: " + reservation.getCheckOutDate();
            resultArea.setText(details);
        });

        add(top, BorderLayout.NORTH);
        add(resultArea, BorderLayout.CENTER);
    }
}
