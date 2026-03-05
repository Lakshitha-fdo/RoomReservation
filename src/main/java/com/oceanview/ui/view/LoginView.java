package com.oceanview.ui.view;

import com.oceanview.client.ClientResult;
import com.oceanview.ui.controller.LoginController;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LoginView extends JFrame {
    private final LoginController loginController;
    private final Runnable onLoginSuccess;

    public LoginView(LoginController loginController, Runnable onLoginSuccess) {
        this.loginController = loginController;
        this.onLoginSuccess = onLoginSuccess;

        setTitle("Ocean View Resort - Login");
        setSize(UiTheme.WINDOW_WIDTH, 300);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel container = new JPanel(new BorderLayout(12, 12));
        UiTheme.addPanelPadding(container);

        JLabel header = new JLabel("Ocean View Resort");
        header.setFont(UiTheme.TITLE_FONT);
        container.add(header, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("Staff Login");
        subtitle.setFont(UiTheme.BODY_FONT);
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        UiTheme.styleTextField(usernameField);
        UiTheme.styleTextField(passwordField);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addFormRow(formPanel, gbc, 0, "Username", usernameField);
        addFormRow(formPanel, gbc, 1, "Password", passwordField);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.add(subtitle, BorderLayout.NORTH);
        centerPanel.add(formPanel, BorderLayout.CENTER);
        container.add(centerPanel, BorderLayout.CENTER);

        JButton loginButton = new JButton("Login");
        UiTheme.styleButton(loginButton);
        loginButton.addActionListener(e -> {
            ClientResult<Void> result = loginController.login(usernameField.getText(), new String(passwordField.getPassword()));
            if (result.success()) {
                JOptionPane.showMessageDialog(this, result.message(), "Login", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                onLoginSuccess.run();
            } else {
                JOptionPane.showMessageDialog(this, result.message(), "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(loginButton);
        container.add(buttonPanel, BorderLayout.SOUTH);
        add(container);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText + ":");
        label.setFont(UiTheme.LABEL_FONT);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.2;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        panel.add(field, gbc);
    }
}
