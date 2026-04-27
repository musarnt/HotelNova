package com.hotelnova.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static Connection connection;

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = AppConfig.getDbUrl();
            String user = AppConfig.getDbUser();
            String password = AppConfig.getDbPassword();

            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}

