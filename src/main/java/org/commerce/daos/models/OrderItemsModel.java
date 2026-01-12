package org.commerce.daos.models;

import java.sql.Connection;
import java.sql.Statement;

public class OrderItemsModel {
    public static void initializeTable(Connection connection){
         String SQL = """
                 CREATE TABLE IF NOT EXISTS order_items(
                 id SERIAL PRIMARY KEY,
                 order_id INT NOT NULL,
                 product_id INT NOT NULL,
                 quantity INT NOT NULL,
                 price DECIMAL(10, 2) NOT NULL,
                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                 FOREIGN KEY (order_id) REFERENCES orders(id),
                 FOREIGN KEY (product_id) REFERENCES products(id)
                 )
                 """;
         try(Statement smt = connection.createStatement()){
             smt.execute(SQL);
             System.out.println("OrderItems table initialized successfully.");
         } catch (Exception e) {
             System.err.println("Failed to initialize OrderItems table: " + e.getMessage());
         }
     }
}
