package com.oceanview.ui.view;

import com.oceanview.client.ClientResult;
import com.oceanview.ui.controller.LoginController;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class LoginView extends JFrame {
    private final LoginController loginController;
    private final Runnable onLoginSuccess;

    public LoginView(LoginController loginController, Runnable onLoginSuccess) {
        this.loginController = loginController;
        this.onLoginSuccess = onLoginSuccess;

        setTitle("Ocean View Resort - Login");
        setSize(420, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel header = new JLabel("Online Room Reservation System", SwingConstants.CENTER);
        add(header, BorderLayout.NORTH);

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        add(formPanel, BorderLayout.CENTER);

        JButton loginButton = new JButton("Login");
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
        add(loginButton, BorderLayout.SOUTH);
    }
}
