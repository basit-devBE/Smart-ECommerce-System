package org.commerce.entities;

import java.sql.Connection;
import java.sql.Statement;

public class Orders {
    public static void initializeTable(Connection connection) {
        String SQL = """
                CREATE TABLE IF NOT EXISTS orders(
                id SERIAL PRIMARY KEY,
                user_id INT NOT NULL,
                order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status VARCHAR(50) DEFAULT 'PENDING',
                total_amount DECIMAL(10, 2) NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """;
        try(Statement smt = connection.createStatement()){
            smt.execute(SQL);
            System.out.println("Orders table initialized successfully.");
        } catch (Exception e) {
            System.err.println("Failed to initialize Orders table: " + e.getMessage());
        }
    }

}
