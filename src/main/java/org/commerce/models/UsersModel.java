package org.commerce.models;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UsersModel {
    public static void initializeTable(Connection connection) throws SQLException{
        String createTableSQL = """
                CREATE TABLE IF NOT EXISTS users(
                id SERIAL PRIMARY KEY,
                firstname VARCHAR(100) NOT NULL,
                lastname VARCHAR(100) NOT NULL,
                phone VARCHAR(15),
                userRole VARCHAR(50) DEFAULT 'CUSTOMER',
                email VARCHAR(100) UNIQUE NOT NULL,
                password VARCHAR(100) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        try(Statement smt = connection.createStatement()){
            smt.execute(createTableSQL);
            System.out.println("Users table initialized successfully.");
        }
    }

}
