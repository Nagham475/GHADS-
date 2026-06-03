package com.ghads.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // ── Singleton instance 
    private static DatabaseConnection instance;
    private Connection connection;

    // ── Connection parameters 
    private static final String URL      = "jdbc:mysql://localhost:3306/ghads_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = "";          // change to your MySQL password

    // ── Private constructor 
    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Connected to ghads_db successfully.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("[DB] MySQL JDBC Driver not found. Add connector JAR to Libraries.", e);
        } catch (SQLException e) {
            throw new RuntimeException("[DB] Failed to connect: " + e.getMessage(), e);
        }
    }

    // ── Public accessor (thread-safe lazy init) 
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("[DB] Connection closed — reconnecting…");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            throw new RuntimeException("[DB] Reconnection failed: " + e.getMessage(), e);
        }
        return connection;
    }

    /** Gracefully close the connection (call on application exit). */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }
}
