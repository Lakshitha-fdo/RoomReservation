package com.oceanview.ui.view;

import com.oceanview.client.ClientResult;
import com.oceanview.model.Bill;
import com.oceanview.ui.controller.BillingController;

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

public class BillDialog extends JDialog {
    public BillDialog(JFrame parent, BillingController billingController) {
        super(parent, "Calculate Bill", true);
        setSize(UiTheme.DIALOG_WIDTH, UiTheme.DIALOG_HEIGHT - 40);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel container = new JPanel(new BorderLayout(12, 12));
        UiTheme.addPanelPadding(container);

        JTextField reservationIdField = new JTextField();
        UiTheme.styleTextField(reservationIdField);
        JButton generateButton = new JButton("Generate Bill");
        UiTheme.styleButton(generateButton);

        JLabel reservationNoValue = new JLabel("-");
        JLabel nightsValue = new JLabel("-");
        JLabel rateValue = new JLabel("-");
        JLabel totalValue = new JLabel("-");

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(new JLabel("Reservation Number:"), BorderLayout.WEST);
        top.add(reservationIdField, BorderLayout.CENTER);
        top.add(generateButton, BorderLayout.EAST);

        JPanel detailPanel = new JPanel(new GridLayout(4, 2, 8, 10));
        detailPanel.add(new JLabel("Reservation No:"));
        detailPanel.add(reservationNoValue);
        detailPanel.add(new JLabel("Nights:"));
        detailPanel.add(nightsValue);
        detailPanel.add(new JLabel("Room Rate:"));
        detailPanel.add(rateValue);
        detailPanel.add(new JLabel("Total Amount:"));
        detailPanel.add(totalValue);

        generateButton.addActionListener(e -> {
            ClientResult<Bill> result = billingController.generateBill(reservationIdField.getText().trim());
            if (!result.success()) {
                JOptionPane.showMessageDialog(this, result.message(), "Error", JOptionPane.ERROR_MESSAGE);
                reservationNoValue.setText("-");
                nightsValue.setText("-");
                rateValue.setText("-");
                totalValue.setText("-");
                return;
            }

            Bill bill = result.data();
            reservationNoValue.setText(bill.getReservationId());
            nightsValue.setText(String.valueOf(bill.getNights()));
            rateValue.setText("$" + String.format("%.2f", bill.getNightlyRate()) + " per night");
            totalValue.setText("$" + String.format("%.2f", bill.getTotal()));
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
