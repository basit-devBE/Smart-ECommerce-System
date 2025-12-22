package org.commerce.models;

import java.sql.Connection;

public class ProductsModel {

    public  static void initializeTable(Connection connection){
        String SQL = """
                CREATE TABLE IF NOT EXISTS products(
                id SERIAL PRIMARY KEY,
                product_name VARCHAR(100) NOT NULL,
                description TEXT,
                price DECIMAL(10, 2) NOT NULL,
                stock INT DEFAULT 0,
                category_id INT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (category_id) REFERENCES categories(id)
                )
                """;

        try(var smt = connection.createStatement()){
            smt.execute(SQL);
            System.out.println("Products table initialized successfully.");
        } catch (Exception e) {
            System.err.println("Failed to initialize Products table: " + e.getMessage());
        }
    }
}
