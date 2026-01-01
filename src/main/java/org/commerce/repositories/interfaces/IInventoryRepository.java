package org.commerce.repositories.interfaces;

import org.commerce.entities.Inventory;
import java.sql.Connection;
import java.util.List;

/**
 * Repository interface for Inventory entity operations.
 */
public interface IInventoryRepository {
    
    /**
     * Creates a new inventory record in the database.
     * 
     * @param inventory The inventory to create
     * @param connection The database connection
     * @return The created inventory with generated ID and timestamps
     */
    Inventory createInventory(Inventory inventory, Connection connection);
    
    /**
     * Updates an existing inventory record.
     * 
     * @param inventory The inventory with updated information
     * @param connection The database connection
     * @return The updated inventory
     */
    Inventory updateInventory(Inventory inventory, Connection connection);
    
    /**
     * Deletes an inventory record by its ID.
     * 
     * @param inventoryId The inventory ID
     * @param connection The database connection
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteInventory(int inventoryId, Connection connection);
    
    /**
     * Retrieves all inventory records for a specific product.
     * 
     * @param productId The product ID
     * @param connection The database connection
     * @return List of inventory records for the product
     */
    List<Inventory> getInventoryByProductId(int productId, Connection connection);
    
    /**
     * Retrieves inventory for a specific product and warehouse location.
     * 
     * @param productId The product ID
     * @param warehouseLocation The warehouse location
     * @param connection The database connection
     * @return The inventory record if found, null otherwise
     */
    Inventory getInventoryByProductAndWarehouse(int productId, String warehouseLocation, Connection connection);
}
