package org.commerce.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.commerce.entities.Product;

public class ProductRepository {
    
    public Product createProduct(Product product, Connection connection){
        String SQL = "INSERT INTO products (product_name, description, price, category_id) VALUES (?, ?, ?, ?) RETURNING *";
        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getDescription());
            pstmt.setBigDecimal(3, product.getPrice());
            pstmt.setInt(4, product.getCategoryId());
            
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                product.setId(rs.getInt("id"));
                product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                System.out.println("Product created successfully.");
                return product;
            }
        }catch(SQLException e){
            System.err.println("Failed to create product: " + e.getMessage());
        }
        return null;
    }
    
    public Product getProductById(int productId, Connection connection){
        String SQL = "SELECT * FROM products WHERE id = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setProductName(rs.getString("product_name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setCategoryId(rs.getInt("category_id"));
                product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                return product;
            }
        }catch(SQLException e){
            System.err.println("Failed to retrieve product: " + e.getMessage());
        }
        return null;
    }
    
    public Product updateProduct(Product product, Connection connection){
        String SQL = "UPDATE products SET product_name = ?, description = ?, price = ?, category_id = ? WHERE id = ? RETURNING *";
        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getDescription());
            pstmt.setBigDecimal(3, product.getPrice());
            pstmt.setInt(4, product.getCategoryId());
            pstmt.setInt(5, product.getId());
            
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                System.out.println("Product updated successfully.");
                return product;
            }
        }catch(SQLException e){
            System.err.println("Failed to update product: " + e.getMessage());
        }
        return null;
    }
    
    public boolean deleteProduct(int productId, Connection connection){
        String SQL = "DELETE FROM products WHERE id = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            pstmt.setInt(1, productId);
            int rowsAffected = pstmt.executeUpdate();
            if(rowsAffected > 0){
                System.out.println("Product deleted successfully.");
                return true;
            }
        }catch(SQLException e){
            System.err.println("Failed to delete product: " + e.getMessage());
        }
        return false;
    }
    
    public int getTotalStock(int productId, Connection connection){
        String SQL = "SELECT COALESCE(SUM(quantity), 0) as total_stock FROM inventory WHERE product_id = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                return rs.getInt("total_stock");
            }
        }catch(SQLException e){
            System.err.println("Failed to get total stock: " + e.getMessage());
        }
        return 0;
    }
    
    public List<Product> getAllProducts(Connection connection){
        String SQL = "SELECT * FROM products";
        List<Product> products = new ArrayList<>();
        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setProductName(rs.getString("product_name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setCategoryId(rs.getInt("category_id"));
                product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                products.add(product);
            }
        }catch(SQLException e){
            System.err.println("Failed to retrieve products: " + e.getMessage());
        }
        return products;
    }
}
