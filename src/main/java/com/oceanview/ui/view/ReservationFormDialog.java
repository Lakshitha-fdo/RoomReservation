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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;

public class ReservationFormDialog extends JDialog {
    public ReservationFormDialog(JFrame parent, ReservationController reservationController) {
        super(parent, "Add Reservation", true);
        setSize(UiTheme.DIALOG_WIDTH, UiTheme.DIALOG_HEIGHT);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel container = new JPanel(new BorderLayout(12, 12));
        UiTheme.addPanelPadding(container);

        JTextField reservationIdField = new JTextField();
        JTextField guestNameField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField contactField = new JTextField();
        JComboBox<RoomType> roomTypeCombo = new JComboBox<>(RoomType.values());
        JTextField checkInField = new JTextField("2025-11-01");
        JTextField checkOutField = new JTextField("2025-11-02");
        UiTheme.styleTextField(reservationIdField);
        UiTheme.styleTextField(guestNameField);
        UiTheme.styleTextField(addressField);
        UiTheme.styleTextField(contactField);
        UiTheme.styleTextField(checkInField);
        UiTheme.styleTextField(checkOutField);
        UiTheme.styleCombo(roomTypeCombo);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, gbc, 0, "Reservation Number", reservationIdField);
        addRow(form, gbc, 1, "Guest Name", guestNameField);
        addRow(form, gbc, 2, "Address", addressField);
        addRow(form, gbc, 3, "Contact Number", contactField);
        addRow(form, gbc, 4, "Room Type", roomTypeCombo);
        addRow(form, gbc, 5, "Check-in (YYYY-MM-DD)", checkInField);
        addRow(form, gbc, 6, "Check-out (YYYY-MM-DD)", checkOutField);

        JButton saveButton = new JButton("Save Reservation");
        JButton closeButton = new JButton("Cancel");
        UiTheme.styleButton(saveButton);
        UiTheme.styleButton(closeButton);

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
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        buttonPanel.add(saveButton);

        container.add(form, BorderLayout.CENTER);
        container.add(buttonPanel, BorderLayout.SOUTH);
        add(container);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, java.awt.Component field) {
        JLabel label = new JLabel(labelText + ":");
        label.setFont(UiTheme.LABEL_FONT);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }
}
