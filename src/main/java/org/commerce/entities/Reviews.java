package org.commerce.entities;


import java.sql.*;

public class Reviews {
    public static void initializeTable(Connection connection) {
        String SQL = """
                CREATE TABLE IF NOT EXISTS reviews(
                id SERIAL PRIMARY KEY,
                product_id INT NOT NULL,
                user_id INT NOT NULL,
                rating INT CHECK (rating >= 1 AND rating <= 5),
                comment TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (product_id) REFERENCES products(id),
                FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """;
        try(Statement smt = connection.createStatement()){
            smt.execute(SQL);
            System.out.println("Reviews table initialized successfully.");
        } catch (Exception e) {
            System.err.println("Failed to initialize Reviews table: " + e.getMessage());
        };
    }
}
