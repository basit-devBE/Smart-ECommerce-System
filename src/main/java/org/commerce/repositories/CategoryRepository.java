package org.commerce.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import  org.commerce.entities.Categories;
public class CategoryRepository {
    public Categories createCategory(Categories category,Connection connection){
        String SQL = "INSERT INTO categories (category_name, description) VALUES (?, ?) RETURNING *";

        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            pstmt.setString(1, category.getCategoryName());
            pstmt.setString(2, category.getDescription());
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                category.setId(rs.getInt("id"));
                category.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                category.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                System.out.println("Category created successfully.");
                return category;
            }
        }catch (SQLException e){
            System.out.println("Failed to create category: " + e.getMessage());
        }
        return null;
    }

    public Categories getCategory(Categories category, Connection connection){
        String SQL = "SELECT * FROM categories WHERE id = ?";

        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            pstmt.setInt(1, category.getId());
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                category.setCategoryName(rs.getString("category_name"));
                category.setDescription(rs.getString("description"));
                category.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                category.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                System.out.println("Category retrieved successfully.");
                return category;
            }
        }catch (SQLException e){
            System.out.println("Failed to retrieve category: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteCategory(Categories category,Connection connection){
        String SQL = "DELETE FROM categories WHERE id = ?";

        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            pstmt.setInt(1, category.getId());
            int rowsAffected = pstmt.executeUpdate();
            if(rowsAffected > 0){
                System.out.println("Category deleted successfully.");
                return true;
            }
        }catch (SQLException e){
            System.out.println("Failed to delete category: " + e.getMessage());
        }
        return false;
    }
    
}
