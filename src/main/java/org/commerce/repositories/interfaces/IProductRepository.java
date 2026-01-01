package org.commerce.repositories.interfaces;

import org.commerce.entities.Product;
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
}
