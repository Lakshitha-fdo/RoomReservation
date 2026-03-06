package com.oceanview.ui.view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
    private static final Color BUTTON_BG = new Color(15, 76, 129);
    private static final Color BUTTON_BG_HOVER = new Color(10, 58, 99);
    private static final Color BUTTON_TEXT = Color.WHITE;

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
        button.setUI(new BasicButtonUI());
        button.setFont(BUTTON_FONT);
        button.setPreferredSize(BUTTON_SIZE);
        button.setFocusPainted(false);
        button.setBackground(BUTTON_BG);
        button.setForeground(BUTTON_TEXT);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(9, 46, 78), 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        button.setRolloverEnabled(true);

        for (var listener : button.getMouseListeners()) {
            if (listener.getClass().getName().contains("UiTheme")) {
                button.removeMouseListener(listener);
            }
        }
        button.addMouseListener(new ButtonHoverListener());
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

    private static final class ButtonHoverListener extends MouseAdapter {
        @Override
        public void mouseEntered(MouseEvent e) {
            if (e.getComponent() instanceof JButton button && button.isEnabled()) {
                button.setBackground(BUTTON_BG_HOVER);
                button.setForeground(BUTTON_TEXT);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (e.getComponent() instanceof JButton button && button.isEnabled()) {
                button.setBackground(BUTTON_BG);
                button.setForeground(BUTTON_TEXT);
            }
        }
    }
}
