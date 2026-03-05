package com.oceanview.dao.jdbc;

import com.oceanview.config.DatabaseConnectionManager;
import com.oceanview.dao.UserDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcUserDao implements UserDao {
    private final DatabaseConnectionManager connectionManager;

    public JdbcUserDao(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public boolean isValidUser(String username, String password) {
        String sql = "SELECT COUNT(1) FROM users WHERE username = ? AND password = ?";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to validate user", e);
        }
    }
}
