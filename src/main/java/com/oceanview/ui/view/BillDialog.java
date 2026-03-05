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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;

public class BillDialog extends JDialog {
    public BillDialog(JFrame parent, BillingController billingController) {
        super(parent, "Calculate Bill", true);
        setSize(500, 280);
        setLocationRelativeTo(parent);

        JTextField reservationIdField = new JTextField();
        JButton generateButton = new JButton("Generate Bill");
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(new JLabel("Reservation Number:"), BorderLayout.WEST);
        top.add(reservationIdField, BorderLayout.CENTER);
        top.add(generateButton, BorderLayout.EAST);

        generateButton.addActionListener(e -> {
            ClientResult<Bill> result = billingController.generateBill(reservationIdField.getText().trim());
            if (!result.success()) {
                JOptionPane.showMessageDialog(this, result.message(), "Error", JOptionPane.ERROR_MESSAGE);
                resultArea.setText("");
                return;
            }

            Bill bill = result.data();
            String details = "Reservation No: " + bill.getReservationId() + "\n"
                    + "Nights: " + bill.getNights() + "\n"
                    + "Room Rate: $" + String.format("%.2f", bill.getNightlyRate()) + " per night\n"
                    + "Total: $" + String.format("%.2f", bill.getTotal());
            resultArea.setText(details);
        });

        add(top, BorderLayout.NORTH);
        add(resultArea, BorderLayout.CENTER);
    }
}
