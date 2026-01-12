package org.commerce.daos.models;

import java.sql.Connection;
import java.sql.Statement;

public class InventoryModel {
    public  static void initializeTable(Connection connection){
        String SQL = """
                CREATE TABLE IF NOT EXISTS inventory(
                id SERIAL PRIMARY KEY,
                product_id INT NOT NULL,
                quantity INT DEFAULT 0,
                warehouse_location VARCHAR(255),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (product_id) REFERENCES products(id),
                UNIQUE(product_id, warehouse_location)
                )
                """;

        try(Statement smt = connection.createStatement()){
            smt.execute(SQL);
            System.out.println("Inventory table initialized successfully.");
        } catch (Exception e) {
            System.err.println("Failed to initialize Inventory table: " + e.getMessage());
        };
    }
}
