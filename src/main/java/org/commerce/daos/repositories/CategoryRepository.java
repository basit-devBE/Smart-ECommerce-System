package org.commerce.daos.repositories;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.commerce.daos.entities.Categories;
import org.commerce.daos.repositories.interfaces.ICategoryRepository;

/**
 * Repository implementation for Category entity operations.
 * Uses BaseRepository to eliminate code duplication.
 */
public class CategoryRepository extends BaseRepository implements ICategoryRepository {
    
    @Override
    public Categories createCategory(Categories category, Connection connection) {
        String SQL = "INSERT INTO categories (category_name, description) VALUES (?, ?) RETURNING *";
        
        return executeInsertReturning(
            connection,
            SQL,
            this::mapCategory,
            category.getCategoryName(),
            category.getDescription()
        );
    }

    @Override
    public Categories getCategoryById(int categoryId, Connection connection) {
        String SQL = "SELECT * FROM categories WHERE id = ?";
        return executeQuerySingle(connection, SQL, this::mapCategory, categoryId);
    }

    @Override
    public List<Categories> getAllCategories(Connection connection) {
        String SQL = "SELECT * FROM categories";
        return executeQueryList(connection, SQL, this::mapCategory);
    }

    @Override
    public Categories updateCategory(Categories category, Connection connection) {
        String SQL = "UPDATE categories SET category_name = ?, description = ? WHERE id = ? RETURNING *";
        
        return executeInsertReturning(
            connection,
            SQL,
            this::mapCategory,
            category.getCategoryName(),
            category.getDescription(),
            category.getId()
        );
    }

    @Override
    public boolean deleteCategory(int categoryId, Connection connection) {
        String SQL = "DELETE FROM categories WHERE id = ?";
        return executeUpdate(connection, SQL, categoryId) > 0;
    }

    @Override
    public boolean exists(int categoryId, Connection connection) {
        String SQL = "SELECT COUNT(*) FROM categories WHERE id = ?";
        return exists(connection, SQL, categoryId);
    }

    /**
     * Maps a ResultSet row to a Categories entity.
     */
    private Categories mapCategory(ResultSet rs) throws SQLException {
        Categories category = new Categories();
        category.setId(rs.getInt("id"));
        category.setCategoryName(rs.getString("category_name"));
        category.setDescription(rs.getString("description"));
        category.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        category.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return category;
    }
}
