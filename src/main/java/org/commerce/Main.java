package org.commerce;

import org.commerce.config.DBConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to the Smart E Commerce Application!");
        DBConfig dbConfig = new DBConfig();
        try (Connection connection = dbConfig.connectDB()) {
            System.out.println("Test Database connection established successfully.");
        } catch (SQLException e) {
            System.out.println("Database connection failed. Please check the configuration and try again." +  e.getMessage());
        }
    }
}
