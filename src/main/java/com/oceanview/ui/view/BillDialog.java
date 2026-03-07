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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.print.PrinterException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BillDialog extends JDialog {
    private final JTextArea receiptPreview;
    private final JLabel reservationNoValue;
    private final JLabel nightsValue;
    private final JLabel rateValue;
    private final JLabel totalValue;
    private Bill currentBill;

    public BillDialog(JFrame parent, BillingController billingController) {
        super(parent, "Print Bill", true);
        setSize(700, 540);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel container = new JPanel(new BorderLayout(14, 14));
        UiTheme.addPanelPadding(container);

        JLabel titleLabel = new JLabel("Billing Console");
        titleLabel.setFont(UiTheme.TITLE_FONT);
        JLabel subtitleLabel = new JLabel("Generate a polished bill preview and print it directly.");
        subtitleLabel.setFont(UiTheme.SUBTITLE_FONT);
        subtitleLabel.setForeground(UiTheme.MUTED_TEXT);

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 4));
        headerText.setOpaque(false);
        headerText.add(titleLabel);
        headerText.add(subtitleLabel);

        JTextField reservationIdField = new JTextField();
        UiTheme.styleTextField(reservationIdField);
        JButton generateButton = new JButton("Generate Bill");
        UiTheme.styleButton(generateButton);

        JPanel requestPanel = new JPanel(new BorderLayout(8, 0));
        requestPanel.setOpaque(false);
        requestPanel.add(new JLabel("Reservation ID:"), BorderLayout.WEST);
        requestPanel.add(reservationIdField, BorderLayout.CENTER);
        requestPanel.add(generateButton, BorderLayout.EAST);

        JPanel header = new JPanel(new BorderLayout(0, 12));
        header.setOpaque(false);
        header.add(headerText, BorderLayout.NORTH);
        header.add(requestPanel, BorderLayout.SOUTH);

        reservationNoValue = createValueLabel("-");
        nightsValue = createValueLabel("-");
        rateValue = createValueLabel("-");
        totalValue = createValueLabel("LKR 0.00");
        totalValue.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));

        JPanel summaryPanel = new JPanel(new GridLayout(4, 2, 12, 12));
        UiTheme.styleCard(summaryPanel);
        summaryPanel.add(createKeyLabel("Reservation No"));
        summaryPanel.add(reservationNoValue);
        summaryPanel.add(createKeyLabel("Nights"));
        summaryPanel.add(nightsValue);
        summaryPanel.add(createKeyLabel("Rate"));
        summaryPanel.add(rateValue);
        summaryPanel.add(createKeyLabel("Total"));
        summaryPanel.add(totalValue);

        receiptPreview = new JTextArea();
        UiTheme.styleResultArea(receiptPreview, 12, 32);
        receiptPreview.setText("""
                Ocean View Resort
                Bill preview will appear here after generation.
                """);
        JScrollPane previewScrollPane = new JScrollPane(receiptPreview);
        previewScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Printable Preview"));

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.add(summaryPanel, BorderLayout.NORTH);
        center.add(previewScrollPane, BorderLayout.CENTER);

        generateButton.addActionListener(e -> generateBill(billingController, reservationIdField.getText().trim()));

        JButton printButton = new JButton("Print Bill");
        JButton closeButton = new JButton("Close");
        UiTheme.styleButton(printButton);
        UiTheme.styleSmallButton(closeButton);
        printButton.addActionListener(e -> printBill());
        closeButton.addActionListener(e -> dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.add(closeButton);
        footer.add(printButton);

        container.add(header, BorderLayout.NORTH);
        container.add(center, BorderLayout.CENTER);
        container.add(footer, BorderLayout.SOUTH);
        add(container);
    }

    private void generateBill(BillingController billingController, String reservationId) {
        ClientResult<Bill> result = billingController.generateBill(reservationId);
        if (!result.success()) {
            JOptionPane.showMessageDialog(this, result.message(), "Generate Bill", JOptionPane.ERROR_MESSAGE);
            currentBill = null;
            reservationNoValue.setText("-");
            nightsValue.setText("-");
            rateValue.setText("-");
            totalValue.setText("LKR 0.00");
            receiptPreview.setText("""
                    Ocean View Resort
                    Bill preview will appear here after generation.
                    """);
            return;
        }

        currentBill = result.data();
        reservationNoValue.setText(currentBill.getReservationId());
        nightsValue.setText(String.valueOf(currentBill.getNights()));
        rateValue.setText("LKR " + String.format("%.2f", currentBill.getNightlyRate()) + " / night");
        totalValue.setText("LKR " + String.format("%.2f", currentBill.getTotal()));
        receiptPreview.setText(buildReceipt(currentBill));
        receiptPreview.setCaretPosition(0);
    }

    private void printBill() {
        if (currentBill == null) {
            JOptionPane.showMessageDialog(this, "Generate a bill first.", "Print Bill", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean printed = receiptPreview.print();
            if (printed) {
                JOptionPane.showMessageDialog(this, "Bill sent to printer.", "Print Bill", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Printing cancelled.", "Print Bill", JOptionPane.WARNING_MESSAGE);
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Print failed: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String buildReceipt(Bill bill) {
        return """
                Ocean View Resort
                Elegant Room Reservation Bill
                Printed: %s
                
                Reservation No : %s
                Nights         : %s
                Room Rate      : LKR %s per night
                
                Total Amount   : LKR %s
                
                Thank you for choosing Ocean View Resort.
                Please present this bill at checkout.
                """.formatted(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                bill.getReservationId(),
                bill.getNights(),
                String.format("%.2f", bill.getNightlyRate()),
                String.format("%.2f", bill.getTotal()));
    }

    private JLabel createKeyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.SUBTITLE_FONT);
        label.setForeground(UiTheme.MUTED_TEXT);
        return label;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.EMPHASIS_FONT);
        return label;
    }
}
