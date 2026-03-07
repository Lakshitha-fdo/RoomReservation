package com.oceanview.ui.view;

import com.oceanview.client.ClientResult;
import com.oceanview.model.Reservation;
import com.oceanview.model.ReservationPage;
import com.oceanview.model.RoomType;
import com.oceanview.ui.controller.ReservationController;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ViewReservationDialog extends JDialog {
    private static final int PAGE_SIZE = 8;

    private final ReservationController reservationController;
    private final JTextField searchField;
    private final DefaultTableModel tableModel;
    private final JTable reservationTable;
    private final JLabel pageLabel;
    private final JLabel resultLabel;
    private final JLabel statusLabel;
    private final JTextField reservationIdField;
    private final JTextField guestNameField;
    private final JTextField addressField;
    private final JTextField contactField;
    private final JComboBox<RoomType> roomTypeCombo;
    private final DatePickerField checkInField;
    private final DatePickerField checkOutField;
    private final JButton updateButton;
    private final JButton previousButton;
    private final JButton nextButton;

    private final List<Reservation> currentReservations = new ArrayList<>();
    private Reservation selectedReservation;
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentSearch = "";

    public ViewReservationDialog(JFrame parent, ReservationController reservationController) {
        super(parent, "Manage Reservations", true);
        this.reservationController = reservationController;

        setSize(980, 620);
        setLocationRelativeTo(parent);
        setResizable(true);

        JPanel container = new JPanel(new BorderLayout(12, 12));
        UiTheme.addPanelPadding(container);

        JLabel titleLabel = new JLabel("Manage Reservations");
        titleLabel.setFont(UiTheme.TITLE_FONT);
        JLabel subtitleLabel = new JLabel("Search, browse, and update room type or stay dates from one screen.");
        subtitleLabel.setFont(UiTheme.SUBTITLE_FONT);
        subtitleLabel.setForeground(UiTheme.MUTED_TEXT);

        searchField = new JTextField();
        UiTheme.styleTextField(searchField);
        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");
        UiTheme.styleSmallButton(searchButton);
        UiTheme.styleSmallButton(clearButton);
        searchButton.addActionListener(e -> performSearch());
        clearButton.addActionListener(e -> clearSearch());
        searchField.addActionListener(e -> performSearch());

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 4));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search reservations:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        JPanel searchActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchActions.setOpaque(false);
        searchActions.add(searchButton);
        searchActions.add(clearButton);
        searchPanel.add(searchActions, BorderLayout.EAST);

        JPanel header = new JPanel(new BorderLayout(0, 12));
        header.setOpaque(false);
        header.add(titlePanel, BorderLayout.NORTH);
        header.add(searchPanel, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Guest", "Contact", "Room Type", "Check-in", "Check-out"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reservationTable = new JTable(tableModel);
        reservationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservationTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateEditorFromSelection();
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(reservationTable);
        UiTheme.styleTable(reservationTable, tableScrollPane);

        previousButton = new JButton("Previous");
        nextButton = new JButton("Next");
        UiTheme.styleSmallButton(previousButton);
        UiTheme.styleSmallButton(nextButton);
        previousButton.addActionListener(e -> changePage(currentPage - 1));
        nextButton.addActionListener(e -> changePage(currentPage + 1));

        pageLabel = new JLabel("Page 1 of 1", SwingConstants.CENTER);
        pageLabel.setFont(UiTheme.BUTTON_FONT);
        resultLabel = new JLabel("No reservations loaded");
        resultLabel.setForeground(UiTheme.MUTED_TEXT);

        JPanel paginationPanel = new JPanel(new BorderLayout(8, 0));
        paginationPanel.setOpaque(false);
        paginationPanel.add(resultLabel, BorderLayout.WEST);

        JPanel pageButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pageButtons.setOpaque(false);
        pageButtons.add(previousButton);
        pageButtons.add(pageLabel);
        pageButtons.add(nextButton);
        paginationPanel.add(pageButtons, BorderLayout.EAST);

        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        UiTheme.styleCard(tablePanel);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);
        tablePanel.add(paginationPanel, BorderLayout.SOUTH);

        reservationIdField = new JTextField();
        guestNameField = new JTextField();
        addressField = new JTextField();
        contactField = new JTextField();
        UiTheme.styleReadOnlyField(reservationIdField);
        UiTheme.styleReadOnlyField(guestNameField);
        UiTheme.styleReadOnlyField(addressField);
        UiTheme.styleReadOnlyField(contactField);

        roomTypeCombo = new JComboBox<>(RoomType.values());
        UiTheme.styleCombo(roomTypeCombo);
        checkInField = new DatePickerField(LocalDate.now());
        checkOutField = new DatePickerField(LocalDate.now().plusDays(1));

        updateButton = new JButton("Update Reservation");
        UiTheme.styleButton(updateButton);
        updateButton.setEnabled(false);
        updateButton.addActionListener(e -> updateReservation());

        JButton refreshButton = new JButton("Refresh");
        UiTheme.styleSmallButton(refreshButton);
        refreshButton.addActionListener(e -> loadReservations(selectedReservation == null ? null : selectedReservation.getReservationId()));

        statusLabel = new JLabel("Select a reservation to update room type or dates.");
        statusLabel.setFont(UiTheme.SUBTITLE_FONT);
        statusLabel.setForeground(UiTheme.MUTED_TEXT);

        JPanel editorPanel = new JPanel(new BorderLayout(10, 10));
        UiTheme.styleCard(editorPanel);

        JLabel editorTitle = new JLabel("Reservation Editor");
        editorTitle.setFont(UiTheme.EMPHASIS_FONT);

        JPanel editorHeader = new JPanel(new BorderLayout());
        editorHeader.setOpaque(false);
        editorHeader.add(editorTitle, BorderLayout.WEST);
        editorHeader.add(refreshButton, BorderLayout.EAST);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(formPanel, gbc, 0, "Reservation ID", reservationIdField);
        addRow(formPanel, gbc, 1, "Guest Name", guestNameField);
        addRow(formPanel, gbc, 2, "Address", addressField);
        addRow(formPanel, gbc, 3, "Contact", contactField);
        addRow(formPanel, gbc, 4, "Room Type", roomTypeCombo);
        addRow(formPanel, gbc, 5, "Check-in Date", checkInField);
        addRow(formPanel, gbc, 6, "Check-out Date", checkOutField);

        JPanel editorFooter = new JPanel(new BorderLayout(0, 8));
        editorFooter.setOpaque(false);
        editorFooter.add(statusLabel, BorderLayout.NORTH);

        JPanel editorActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        editorActions.setOpaque(false);
        editorActions.add(updateButton);
        editorFooter.add(editorActions, BorderLayout.SOUTH);

        editorPanel.add(editorHeader, BorderLayout.NORTH);
        editorPanel.add(formPanel, BorderLayout.CENTER);
        editorPanel.add(editorFooter, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, editorPanel);
        splitPane.setResizeWeight(0.62);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        JButton closeButton = new JButton("Close");
        UiTheme.styleSmallButton(closeButton);
        closeButton.addActionListener(e -> dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(closeButton);

        container.add(header, BorderLayout.NORTH);
        container.add(splitPane, BorderLayout.CENTER);
        container.add(footer, BorderLayout.SOUTH);
        add(container);

        loadReservations(null);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, java.awt.Component component) {
        JLabel label = new JLabel(labelText + ":");
        label.setFont(UiTheme.LABEL_FONT);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.25;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        panel.add(component, gbc);
    }

    private void performSearch() {
        currentSearch = searchField.getText().trim();
        currentPage = 1;
        loadReservations(null);
    }

    private void clearSearch() {
        searchField.setText("");
        currentSearch = "";
        currentPage = 1;
        loadReservations(null);
    }

    private void changePage(int page) {
        if (page < 1 || page > totalPages) {
            return;
        }
        currentPage = page;
        loadReservations(null);
    }

    private void loadReservations(String reservationIdToSelect) {
        ClientResult<ReservationPage> result = reservationController.findReservations(currentSearch, currentPage, PAGE_SIZE);
        if (!result.success()) {
            JOptionPane.showMessageDialog(this, result.message(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ReservationPage reservationPage = result.data();
        currentPage = reservationPage.getCurrentPage();
        totalPages = reservationPage.getTotalPages();
        currentReservations.clear();
        currentReservations.addAll(reservationPage.getReservations());

        tableModel.setRowCount(0);
        for (Reservation reservation : currentReservations) {
            tableModel.addRow(new Object[]{
                    reservation.getReservationId(),
                    reservation.getGuestName(),
                    reservation.getContactNumber(),
                    reservation.getRoomType().name(),
                    reservation.getCheckInDate(),
                    reservation.getCheckOutDate()
            });
        }

        resultLabel.setText(reservationPage.getTotalItems() + " reservation(s)");
        pageLabel.setText("Page " + currentPage + " of " + totalPages);
        previousButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);

        if (currentReservations.isEmpty()) {
            clearEditor();
            statusLabel.setText("No reservations match the current search.");
            return;
        }

        int rowToSelect = 0;
        if (reservationIdToSelect != null) {
            for (int i = 0; i < currentReservations.size(); i++) {
                if (reservationIdToSelect.equals(currentReservations.get(i).getReservationId())) {
                    rowToSelect = i;
                    break;
                }
            }
        }
        reservationTable.setRowSelectionInterval(rowToSelect, rowToSelect);
    }

    private void populateEditorFromSelection() {
        int selectedRow = reservationTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentReservations.size()) {
            clearEditor();
            return;
        }

        selectedReservation = currentReservations.get(selectedRow);
        reservationIdField.setText(selectedReservation.getReservationId());
        guestNameField.setText(selectedReservation.getGuestName());
        addressField.setText(selectedReservation.getAddress());
        contactField.setText(selectedReservation.getContactNumber());
        roomTypeCombo.setSelectedItem(selectedReservation.getRoomType());
        checkInField.setDate(selectedReservation.getCheckInDate());
        checkOutField.setDate(selectedReservation.getCheckOutDate());
        updateButton.setEnabled(true);
        statusLabel.setText("Editing reservation " + selectedReservation.getReservationId() + ".");
    }

    private void clearEditor() {
        selectedReservation = null;
        reservationIdField.setText("");
        guestNameField.setText("");
        addressField.setText("");
        contactField.setText("");
        roomTypeCombo.setSelectedIndex(0);
        checkInField.setDate(LocalDate.now());
        checkOutField.setDate(LocalDate.now().plusDays(1));
        updateButton.setEnabled(false);
    }

    private void updateReservation() {
        if (selectedReservation == null) {
            JOptionPane.showMessageDialog(this, "Select a reservation first.", "Update Reservation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Reservation updatedReservation = new Reservation(
                    selectedReservation.getReservationId(),
                    selectedReservation.getGuestName(),
                    selectedReservation.getAddress(),
                    selectedReservation.getContactNumber(),
                    (RoomType) roomTypeCombo.getSelectedItem(),
                    checkInField.getDate(),
                    checkOutField.getDate());

            ClientResult<Void> result = reservationController.updateReservation(updatedReservation);
            if (!result.success()) {
                JOptionPane.showMessageDialog(this, result.message(), "Update Reservation", JOptionPane.ERROR_MESSAGE);
                return;
            }

            statusLabel.setText(result.message());
            updateButton.setEnabled(true);
            loadReservations(updatedReservation.getReservationId());
            JOptionPane.showMessageDialog(this, result.message(), "Update Reservation", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid reservation update.", "Update Reservation", JOptionPane.ERROR_MESSAGE);
        }
    }
}
