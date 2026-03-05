package com.oceanview.ui.view;

import com.oceanview.client.ClientResult;
import com.oceanview.model.Reservation;
import com.oceanview.model.RoomType;
import com.oceanview.ui.controller.ReservationController;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;

public class ReservationFormDialog extends JDialog {
    public ReservationFormDialog(JFrame parent, ReservationController reservationController) {
        super(parent, "Add Reservation", true);
        setSize(460, 360);
        setLocationRelativeTo(parent);

        JTextField reservationIdField = new JTextField();
        JTextField guestNameField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField contactField = new JTextField();
        JComboBox<RoomType> roomTypeCombo = new JComboBox<>(RoomType.values());
        JTextField checkInField = new JTextField("2025-11-01");
        JTextField checkOutField = new JTextField("2025-11-02");

        JPanel form = new JPanel(new GridLayout(7, 2, 8, 8));
        form.add(new JLabel("Reservation Number:"));
        form.add(reservationIdField);
        form.add(new JLabel("Guest Name:"));
        form.add(guestNameField);
        form.add(new JLabel("Address:"));
        form.add(addressField);
        form.add(new JLabel("Contact Number:"));
        form.add(contactField);
        form.add(new JLabel("Room Type:"));
        form.add(roomTypeCombo);
        form.add(new JLabel("Check-in Date (YYYY-MM-DD):"));
        form.add(checkInField);
        form.add(new JLabel("Check-out Date (YYYY-MM-DD):"));
        form.add(checkOutField);

        JButton saveButton = new JButton("Save Reservation");
        saveButton.addActionListener(e -> {
            try {
                Reservation reservation = new Reservation(
                        reservationIdField.getText().trim(),
                        guestNameField.getText().trim(),
                        addressField.getText().trim(),
                        contactField.getText().trim(),
                        (RoomType) roomTypeCombo.getSelectedItem(),
                        LocalDate.parse(checkInField.getText().trim()),
                        LocalDate.parse(checkOutField.getText().trim()));

                ClientResult<Void> result = reservationController.addReservation(reservation);
                if (result.success()) {
                    JOptionPane.showMessageDialog(this, result.message(), "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, result.message(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Use format YYYY-MM-DD for dates.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(form, BorderLayout.CENTER);
        add(saveButton, BorderLayout.SOUTH);
    }
}
