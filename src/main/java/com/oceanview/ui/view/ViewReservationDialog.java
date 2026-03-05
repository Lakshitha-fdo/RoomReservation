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
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class ViewReservationDialog extends JDialog {
    public ViewReservationDialog(JFrame parent, ReservationController reservationController) {
        super(parent, "View Reservation", true);
        setSize(UiTheme.DIALOG_WIDTH, UiTheme.DIALOG_HEIGHT);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel container = new JPanel(new BorderLayout(12, 12));
        UiTheme.addPanelPadding(container);

        JTextField reservationIdField = new JTextField();
        UiTheme.styleTextField(reservationIdField);
        JButton searchButton = new JButton("Search");
        UiTheme.styleButton(searchButton);

        JLabel reservationNoValue = new JLabel("-");
        JLabel guestNameValue = new JLabel("-");
        JLabel addressValue = new JLabel("-");
        JLabel contactValue = new JLabel("-");
        JLabel roomTypeValue = new JLabel("-");
        JLabel checkInValue = new JLabel("-");
        JLabel checkOutValue = new JLabel("-");

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(new JLabel("Reservation Number:"), BorderLayout.WEST);
        top.add(reservationIdField, BorderLayout.CENTER);
        top.add(searchButton, BorderLayout.EAST);

        JPanel detailPanel = new JPanel(new GridLayout(7, 2, 8, 10));
        detailPanel.add(new JLabel("Reservation No:"));
        detailPanel.add(reservationNoValue);
        detailPanel.add(new JLabel("Guest Name:"));
        detailPanel.add(guestNameValue);
        detailPanel.add(new JLabel("Address:"));
        detailPanel.add(addressValue);
        detailPanel.add(new JLabel("Contact Number:"));
        detailPanel.add(contactValue);
        detailPanel.add(new JLabel("Room Type:"));
        detailPanel.add(roomTypeValue);
        detailPanel.add(new JLabel("Check-in Date:"));
        detailPanel.add(checkInValue);
        detailPanel.add(new JLabel("Check-out Date:"));
        detailPanel.add(checkOutValue);

        searchButton.addActionListener(e -> {
            ClientResult<Reservation> result = reservationController.findReservation(reservationIdField.getText().trim());
            if (!result.success()) {
                JOptionPane.showMessageDialog(this, result.message(), "Not Found", JOptionPane.ERROR_MESSAGE);
                reservationNoValue.setText("-");
                guestNameValue.setText("-");
                addressValue.setText("-");
                contactValue.setText("-");
                roomTypeValue.setText("-");
                checkInValue.setText("-");
                checkOutValue.setText("-");
                return;
            }

            Reservation reservation = result.data();
            reservationNoValue.setText(reservation.getReservationId());
            guestNameValue.setText(reservation.getGuestName());
            addressValue.setText(reservation.getAddress());
            contactValue.setText(reservation.getContactNumber());
            roomTypeValue.setText(reservation.getRoomType().name());
            checkInValue.setText(reservation.getCheckInDate().toString());
            checkOutValue.setText(reservation.getCheckOutDate().toString());
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        UiTheme.styleButton(closeButton);
        closeButton.addActionListener(e -> dispose());
        bottom.add(closeButton);

        container.add(top, BorderLayout.NORTH);
        container.add(detailPanel, BorderLayout.CENTER);
        container.add(bottom, BorderLayout.SOUTH);
        add(container);
    }
}
