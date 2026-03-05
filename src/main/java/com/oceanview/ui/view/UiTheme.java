package com.oceanview.ui.view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public final class UiTheme {
    public static final int WINDOW_WIDTH = 560;
    public static final int WINDOW_HEIGHT = 420;
    public static final int DIALOG_WIDTH = 560;
    public static final int DIALOG_HEIGHT = 420;

    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);

    private static final Dimension FIELD_SIZE = new Dimension(290, 32);
    private static final Dimension BUTTON_SIZE = new Dimension(150, 36);

    private UiTheme() {
    }

    public static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // If look and feel cannot be applied, Swing falls back to default.
        }
        UIManager.put("Label.font", LABEL_FONT);
        UIManager.put("TextField.font", BODY_FONT);
        UIManager.put("PasswordField.font", BODY_FONT);
        UIManager.put("Button.font", BUTTON_FONT);
        UIManager.put("ComboBox.font", BODY_FONT);
        UIManager.put("TextArea.font", BODY_FONT);
    }

    public static void styleTextField(JTextField field) {
        field.setFont(BODY_FONT);
        field.setPreferredSize(FIELD_SIZE);
    }

    public static void styleCombo(JComboBox<?> comboBox) {
        comboBox.setFont(BODY_FONT);
        comboBox.setPreferredSize(FIELD_SIZE);
    }

    public static void styleButton(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setPreferredSize(BUTTON_SIZE);
        button.setFocusPainted(false);
        button.setBackground(new Color(17, 96, 156));
        button.setForeground(Color.WHITE);
    }

    public static void styleResultArea(JTextArea textArea, int rows, int cols) {
        textArea.setFont(BODY_FONT);
        textArea.setRows(rows);
        textArea.setColumns(cols);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBackground(new Color(248, 250, 252));
        textArea.setBorder(BorderFactory.createLineBorder(new Color(210, 216, 224)));
    }

    public static void addPanelPadding(JComponent component) {
        component.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    }
}
