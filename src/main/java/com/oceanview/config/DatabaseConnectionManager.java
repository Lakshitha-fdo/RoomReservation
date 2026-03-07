package com.oceanview.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConnectionManager {
    private static final String DEFAULT_DB_URL = "jdbc:sqlite:data/ocean_view.db";
    private static volatile DatabaseConnectionManager instance;

    private final String dbUrl;

    private DatabaseConnectionManager(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public static DatabaseConnectionManager getInstance() {
        return getInstance(System.getProperty("db.url", DEFAULT_DB_URL));
    }

    public static synchronized DatabaseConnectionManager getInstance(String dbUrl) {
        if (instance == null) {
            instance = new DatabaseConnectionManager(dbUrl);
            instance.initializeSchema();
        }
        return instance;
    }

    public static synchronized void resetForTests() {
        instance = null;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    private void initializeSchema() {
        
        File dbFolder = new File("data");
        if (!dbFolder.exists()) {
            dbFolder.mkdirs();
        }

        String sql = loadSchemaSql();
        String[] statements = sql.split(";");

        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            for (String raw : statements) {
                String trimmed = raw.trim();
                if (!trimmed.isEmpty()) {
                    statement.execute(trimmed);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize database schema", e);
        }
    }

    private String loadSchemaSql() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("schema.sql");
        if (inputStream == null) {
            throw new IllegalStateException("schema.sql not found in resources");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            return builder.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read schema.sql", e);
        }
    }
}
