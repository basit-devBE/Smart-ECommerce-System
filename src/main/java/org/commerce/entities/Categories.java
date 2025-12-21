package org.commerce.entities;

import java.sql.Connection;
import java.sql.Statement;

public class Categories {

    public static void initializeTable(Connection connection){
        String SQL = """
                CREATE TABLE IF NOT EXISTS categories(
                id SERIAL PRIMARY KEY,
                category_name VARCHAR(100) NOT NULL,
                description TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        try(Statement smt = connection.createStatement()){
            smt.execute(SQL);
            System.out.println("Categories table initialized successfully.");
        } catch (Exception e) {
            System.err.println("Failed to initialize Categories table: " + e.getMessage());
        }
    }
}
