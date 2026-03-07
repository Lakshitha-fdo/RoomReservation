package com.oceanview.ui.view;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class DatePickerField extends JPanel {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final JTextField textField;
    private final JButton pickerButton;
    private LocalDate selectedDate;

    public DatePickerField(LocalDate initialDate) {
        super(new BorderLayout(8, 0));
        this.selectedDate = initialDate;
        setOpaque(false);

        textField = new JTextField();
        UiTheme.styleReadOnlyField(textField);
        textField.setHorizontalAlignment(SwingConstants.LEFT);

        pickerButton = new JButton("Pick");
        UiTheme.styleSmallButton(pickerButton);
        pickerButton.addActionListener(e -> openCalendarPopup());

        add(textField, BorderLayout.CENTER);
        add(pickerButton, BorderLayout.EAST);
        setDate(initialDate);
    }

    public LocalDate getDate() {
        return selectedDate;
    }

    public void setDate(LocalDate date) {
        selectedDate = date;
        textField.setText(date == null ? "" : DATE_FORMATTER.format(date));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        textField.setEnabled(enabled);
        pickerButton.setEnabled(enabled);
    }

    private void openCalendarPopup() {
        LocalDate initialDate = selectedDate == null ? LocalDate.now() : selectedDate;
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBorder(javax.swing.BorderFactory.createLineBorder(UiTheme.BORDER_COLOR));
        popupMenu.add(new CalendarPanel(initialDate, date -> {
            setDate(date);
            popupMenu.setVisible(false);
        }));
        popupMenu.show(this, 0, getHeight());
    }

    private static final class CalendarPanel extends JPanel {
        private final JLabel monthLabel;
        private final JPanel dayGrid;
        private final DateSelectionListener selectionListener;
        private LocalDate selectedDate;
        private YearMonth visibleMonth;

        private CalendarPanel(LocalDate initialDate, DateSelectionListener selectionListener) {
            super(new BorderLayout(8, 8));
            this.selectionListener = selectionListener;
            this.selectedDate = initialDate;
            this.visibleMonth = YearMonth.from(initialDate);

            setOpaque(true);
            setBackground(UiTheme.CARD_BG);
            setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JButton previousButton = createNavButton("<");
            JButton nextButton = createNavButton(">");
            monthLabel = new JLabel("", SwingConstants.CENTER);
            monthLabel.setFont(UiTheme.BUTTON_FONT);

            previousButton.addActionListener(e -> {
                visibleMonth = visibleMonth.minusMonths(1);
                rebuildCalendar();
            });
            nextButton.addActionListener(e -> {
                visibleMonth = visibleMonth.plusMonths(1);
                rebuildCalendar();
            });

            JPanel header = new JPanel(new BorderLayout(8, 0));
            header.setOpaque(false);
            header.add(previousButton, BorderLayout.WEST);
            header.add(monthLabel, BorderLayout.CENTER);
            header.add(nextButton, BorderLayout.EAST);

            JPanel weekdayHeader = new JPanel(new GridLayout(1, 7, 4, 4));
            weekdayHeader.setOpaque(false);
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                JLabel label = new JLabel(dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH), SwingConstants.CENTER);
                label.setFont(UiTheme.SUBTITLE_FONT);
                label.setForeground(UiTheme.MUTED_TEXT);
                weekdayHeader.add(label);
            }

            dayGrid = new JPanel(new GridLayout(6, 7, 4, 4));
            dayGrid.setOpaque(false);

            JPanel center = new JPanel(new BorderLayout(6, 6));
            center.setOpaque(false);
            center.add(weekdayHeader, BorderLayout.NORTH);
            center.add(dayGrid, BorderLayout.CENTER);

            add(header, BorderLayout.NORTH);
            add(center, BorderLayout.CENTER);
            rebuildCalendar();
        }

        private void rebuildCalendar() {
            monthLabel.setText(visibleMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + visibleMonth.getYear());
            dayGrid.removeAll();

            LocalDate firstOfMonth = visibleMonth.atDay(1);
            int leadingBlanks = firstOfMonth.getDayOfWeek().getValue() - 1;
            int daysInMonth = visibleMonth.lengthOfMonth();

            for (int i = 0; i < leadingBlanks; i++) {
                dayGrid.add(new JLabel());
            }

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = visibleMonth.atDay(day);
                JButton button = new JButton(String.valueOf(day));
                button.setFocusPainted(false);
                button.setBorder(javax.swing.BorderFactory.createLineBorder(UiTheme.BORDER_COLOR));
                button.setBackground(date.equals(selectedDate) ? UiTheme.SUCCESS_BG : Color.WHITE);
                button.setForeground(Color.BLACK);
                button.addActionListener(e -> selectionListener.onDateSelected(date));
                dayGrid.add(button);
            }

            int cells = leadingBlanks + daysInMonth;
            for (int i = cells; i < 42; i++) {
                dayGrid.add(new JLabel());
            }

            revalidate();
            repaint();
        }

        private JButton createNavButton(String text) {
            JButton button = new JButton(text);
            button.setFont(UiTheme.BUTTON_FONT);
            button.setPreferredSize(new java.awt.Dimension(48, 30));
            button.setFocusPainted(false);
            return button;
        }
    }

    @FunctionalInterface
    private interface DateSelectionListener {
        void onDateSelected(LocalDate date);
    }
}
