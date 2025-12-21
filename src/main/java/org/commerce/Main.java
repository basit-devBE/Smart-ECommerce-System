package org.commerce;

import org.commerce.config.DBConfig;
import org.commerce.entities.*;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    
    public static void main(String[] args) {
        System.out.println("Welcome to the Smart E Commerce Application!");
        DBConfig dbConfig = new DBConfig();
        try (Connection connection = dbConfig.connectDB()) {
            System.out.println("Test Database connection established successfully.");

            Users.initializeTable(connection);
            Categories.initializeTable(connection);
            Products.initializeTable(connection);
            Inventory.initializeTable(connection);
            Orders.initializeTable(connection);
            OrderItems.initializeTable(connection);
            Reviews.initializeTable(connection);

            System.out.println("Application setup completed successfully.");
        } catch (SQLException e) {
            System.out.println("Database connection failed. Please check the configuration and try again." +  e.getMessage());
        }
    }
}
