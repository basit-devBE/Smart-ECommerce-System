package org.commerce.daos.repositories.interfaces;

import org.commerce.daos.entities.Product;
import java.sql.Connection;
import java.util.List;

/**
 * Repository interface for Product entity operations.
 */
public interface IProductRepository {
    
    /**
     * Creates a new product in the database.
     * 
     * @param product The product to create
     * @param connection The database connection
     * @return The created product with generated ID and timestamps
     */
    Product createProduct(Product product, Connection connection);
    
    /**
     * Retrieves a product by its ID.
     * 
     * @param productId The product ID
     * @param connection The database connection
     * @return The product if found, null otherwise
     */
    Product getProductById(int productId, Connection connection);
    
    /**
     * Retrieves all products from the database.
     * 
     * @param connection The database connection
     * @return List of all products
     */
    List<Product> getAllProducts(Connection connection);
    
    /**
     * Updates an existing product.
     * 
     * @param product The product with updated information
     * @param connection The database connection
     * @return The updated product
     */
    Product updateProduct(Product product, Connection connection);
    
    /**
     * Deletes a product by its ID.
     * 
     * @param productId The product ID
     * @param connection The database connection
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteProduct(int productId, Connection connection);
    
    /**
     * Gets the total stock quantity for a product.
     * 
     * @param productId The product ID
     * @param connection The database connection
     * @return Total stock quantity
     */
    int getTotalStock(int productId, Connection connection);
    
    /**
     * Checks if a product with the given name exists.
     * 
     * @param productName The product name to check
     * @param connection The database connection
     * @return true if product exists, false otherwise
     */
    boolean existsByName(String productName, Connection connection);
    
    /**
     * Searches products by name or description (case-insensitive, optimized with indexes).
     * 
     * @param searchTerm The search term to match against product name or description
     * @param connection The database connection
     * @return List of products matching the search term
     */
    List<Product> searchProducts(String searchTerm, Connection connection);
    
    /**
     * Searches products by category and optional search term (optimized).
     * 
     * @param categoryId The category ID to filter by (null for all categories)
     * @param searchTerm The search term (null or empty for no search filter)
     * @param connection The database connection
     * @return List of products matching the criteria
     */
    List<Product> searchProductsByCategory(Integer categoryId, String searchTerm, Connection connection);
}
