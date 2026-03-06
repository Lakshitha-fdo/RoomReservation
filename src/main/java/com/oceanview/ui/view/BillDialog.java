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
import java.awt.print.PrinterException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        Bill[] currentBill = new Bill[1];

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
                currentBill[0] = null;
                return;
            }

            Bill bill = result.data();
            currentBill[0] = bill;
            reservationNoValue.setText(bill.getReservationId());
            nightsValue.setText(String.valueOf(bill.getNights()));
            rateValue.setText("LKR" + String.format("%.2f", bill.getNightlyRate()) + " per night");
            totalValue.setText("LKR" + String.format("%.2f", bill.getTotal()));
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton printButton = new JButton("Print Bill");
        JButton closeButton = new JButton("Close");
        UiTheme.styleButton(printButton);
        UiTheme.styleButton(closeButton);
        printButton.addActionListener(e -> printBill(currentBill[0]));
        closeButton.addActionListener(e -> dispose());
        bottom.add(printButton);
        bottom.add(closeButton);

        container.add(top, BorderLayout.NORTH);
        container.add(detailPanel, BorderLayout.CENTER);
        container.add(bottom, BorderLayout.SOUTH);
        add(container);
    }

    private void printBill(Bill bill) {
        if (bill == null) {
            JOptionPane.showMessageDialog(this, "Generate a bill first.", "Print Bill", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String receipt = "Ocean View Resort\n"
                + "Room Reservation Bill\n"
                + "Printed: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n"
                + "----------------------------------------\n"
                + "Reservation No: " + bill.getReservationId() + "\n"
                + "Nights: " + bill.getNights() + "\n"
                + "Room Rate: LKR" + String.format("%.2f", bill.getNightlyRate()) + " per night\n"
                + "Total Amount: LKR" + String.format("%.2f", bill.getTotal()) + "\n"
                + "----------------------------------------\n"
                + "Thank you.\n";

        javax.swing.JTextArea printerArea = new javax.swing.JTextArea(receipt);
        printerArea.setFont(UiTheme.BODY_FONT);

        try {
            boolean printed = printerArea.print();
            if (printed) {
                JOptionPane.showMessageDialog(this, "Bill sent to printer.", "Print Bill", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Printing cancelled.", "Print Bill", JOptionPane.WARNING_MESSAGE);
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Print failed: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
