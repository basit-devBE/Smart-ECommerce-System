package org.commerce.config;
import java.sql.*;

public class DBConfig {
    private final String dbHost = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
    private final String dbPort = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "5432";
    private final String dbName = System.getenv("DB_NAME");
    private final String username = System.getenv("DB_USER");
    private final String password = System.getenv("DB_PASSWORD");

    public Connection connectDB() throws SQLException {
        if (dbName == null || username == null || password == null) {
            throw new SQLException("Database configuration is incomplete. Please set DB_NAME, DB_USER, and DB_PASSWORD environment variables.");
        }
        
        String url = String.format("jdbc:postgresql://%s:%s/%s", dbHost, dbPort, dbName);
        
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            throw e;
        }
    }

}
