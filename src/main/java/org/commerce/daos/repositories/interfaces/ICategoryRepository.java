package org.commerce.daos.repositories.interfaces;

import org.commerce.daos.entities.Categories;
import java.sql.Connection;
import java.util.List;

/**
 * Repository interface for Category entity operations.
 */
public interface ICategoryRepository {
    
    /**
     * Creates a new category in the database.
     * 
     * @param category The category to create
     * @param connection The database connection
     * @return The created category with generated ID and timestamps
     */
    Categories createCategory(Categories category, Connection connection);
    
    /**
     * Retrieves a category by its ID.
     * 
     * @param categoryId The category ID
     * @param connection The database connection
     * @return The category if found, null otherwise
     */
    Categories getCategoryById(int categoryId, Connection connection);
    
    /**
     * Retrieves all categories from the database.
     * 
     * @param connection The database connection
     * @return List of all categories
     */
    List<Categories> getAllCategories(Connection connection);
    
    /**
     * Updates an existing category.
     * 
     * @param category The category with updated information
     * @param connection The database connection
     * @return The updated category
     */
    Categories updateCategory(Categories category, Connection connection);
    
    /**
     * Deletes a category by its ID.
     * 
     * @param categoryId The category ID
     * @param connection The database connection
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteCategory(int categoryId, Connection connection);
    
    /**
     * Checks if a category exists.
     * 
     * @param categoryId The category ID
     * @param connection The database connection
     * @return true if category exists, false otherwise
     */
    boolean exists(int categoryId, Connection connection);
}
