package org.commerce.services;

import org.commerce.common.Result;
import org.commerce.common.ValidationResult;
import org.commerce.entities.Inventory;
import org.commerce.exceptions.EntityNotFoundException;
import org.commerce.exceptions.ServiceException;
import org.commerce.repositories.InventoryRepository;
import org.commerce.repositories.ProductRepository;
import org.commerce.repositories.interfaces.IInventoryRepository;
import org.commerce.repositories.interfaces.IProductRepository;
import org.commerce.validators.InventoryValidator;

import java.sql.Connection;
import java.util.List;

/**
 * Service layer for Inventory business logic.
 * Handles validation, business rules, and delegates to repository.
 * Invalidates product stock cache when inventory changes.
 */
public class InventoryService {
    private final Connection connection;
    private final IInventoryRepository inventoryRepository;
    private final IProductRepository productRepository;
    private ProductService productService; // For cache invalidation

    public InventoryService(Connection connection) {
        this.connection = connection;
        this.inventoryRepository = new InventoryRepository();
        this.productRepository = new ProductRepository();
    }
    
    /**
     * Sets the ProductService reference for cache invalidation.
     * Should be called after both services are instantiated.
     * 
     * @param productService The ProductService instance
     */
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Creates a new inventory record.
     * 
     * @param inventory The inventory to create
     * @return Result containing the created inventory or error message
     */
    public Result<Inventory> createInventory(Inventory inventory) {
        // Field validation
        ValidationResult validation = InventoryValidator.validate(inventory);
        if (!validation.isValid()) {
            return Result.failure(validation.getErrorMessage());
        }

        // Business rule: Product must exist
        if (productRepository.getProductById(inventory.getProductId(), connection) == null) {
            throw new ServiceException("Product with ID " + inventory.getProductId() + " does not exist");
        }

        // Business rule: Check if inventory already exists for this product and warehouse
        Inventory existing = inventoryRepository.getInventoryByProductAndWarehouse(
            inventory.getProductId(), 
            inventory.getWarehouseLocation(), 
            connection
        );
        
        if (existing != null) {
            throw new ServiceException(
                "Inventory already exists for product " + inventory.getProductId() + 
                " at warehouse " + inventory.getWarehouseLocation()
            );
        }

        // Create inventory
        Inventory created = inventoryRepository.createInventory(inventory, connection);
        
        // Invalidate product stock cache
        if (productService != null) {
            productService.invalidateStockCache(inventory.getProductId());
        }
        
        return Result.success(created, "Inventory created successfully");
    }

    /**
     * Updates an existing inventory record.
     * 
     * @param inventory The inventory with updated information
     * @return Result containing the updated inventory or error message
     */
    public Result<Inventory> updateInventory(Inventory inventory) {
        // Field validation
        ValidationResult validation = InventoryValidator.validateForUpdate(inventory);
        if (!validation.isValid()) {
            return Result.failure(validation.getErrorMessage());
        }

        // Business rule: Inventory must exist
        Inventory existingInventory = inventoryRepository.getInventoryByProductAndWarehouse(
            inventory.getProductId(),
            inventory.getWarehouseLocation(),
            connection
        );
        
        if (existingInventory == null) {
            throw new EntityNotFoundException(
                "Inventory for product " + inventory.getProductId() + 
                " at warehouse " + inventory.getWarehouseLocation()
            );
        }

        // Set the ID from existing record
        inventory.setId(existingInventory.getId());

        Inventory updated = inventoryRepository.updateInventory(inventory, connection);
        
        // Invalidate product stock cache
        if (productService != null) {
            productService.invalidateStockCache(inventory.getProductId());
        }
        
        return Result.success(updated, "Inventory updated successfully");
    }

    /**
     * Deletes an inventory record by ID.
     * 
     * @param inventoryId The inventory ID
     * @return Result containing success status or error message
     */
    public Result<Boolean> deleteInventory(int inventoryId) {
        if (inventoryId <= 0) {
            return Result.failure("Invalid inventory ID");
        }
        
        // Get inventory before deletion to know which product to invalidate
        Inventory inventory = inventoryRepository.getInventoryById(inventoryId, connection);
        
        boolean deleted = inventoryRepository.deleteInventory(inventoryId, connection);
        if (!deleted) {
            throw new EntityNotFoundException("Inventory", inventoryId);
        }
        
        // Invalidate product stock cache if inventory was found
        if (inventory != null && productService != null) {
            productService.invalidateStockCache(inventory.getProductId());
        }

        return Result.success(deleted, "Inventory deleted successfully");
    }

    /**
     * Retrieves all inventory records for a specific product.
     * 
     * @param productId The product ID
     * @return Result containing list of inventory records
     */
    public Result<List<Inventory>> getInventoryByProductId(int productId) {
        if (productId <= 0) {
            return Result.failure("Invalid product ID");
        }

        // Business rule: Product must exist
        if (productRepository.getProductById(productId, connection) == null) {
            throw new EntityNotFoundException("Product", productId);
        }

        List<Inventory> inventories = inventoryRepository.getInventoryByProductId(productId, connection);
        return Result.success(inventories);
    }

    /**
     * Retrieves inventory for a specific product and warehouse.
     * 
     * @param productId The product ID
     * @param warehouseLocation The warehouse location
     * @return Result containing the inventory or error message
     */
    public Result<Inventory> getInventoryByProductAndWarehouse(int productId, String warehouseLocation) {
        if (productId <= 0) {
            return Result.failure("Invalid product ID");
        }

        if (warehouseLocation == null || warehouseLocation.trim().isEmpty()) {
            return Result.failure("Warehouse location is required");
        }

        Inventory inventory = inventoryRepository.getInventoryByProductAndWarehouse(
            productId, 
            warehouseLocation, 
            connection
        );

        if (inventory == null) {
            throw new EntityNotFoundException(
                "Inventory for product " + productId + " at warehouse " + warehouseLocation
            );
        }

        return Result.success(inventory);
    }

    /**
     * Adjusts inventory quantity (add or subtract).
     * 
     * @param productId The product ID
     * @param warehouseLocation The warehouse location
     * @param quantityChange The quantity to add (positive) or subtract (negative)
     * @return Result containing the updated inventory
     */
    public Result<Inventory> adjustInventory(int productId, String warehouseLocation, int quantityChange) {
        Inventory inventory = inventoryRepository.getInventoryByProductAndWarehouse(
            productId, 
            warehouseLocation, 
            connection
        );

        if (inventory == null) {
            throw new EntityNotFoundException(
                "Inventory for product " + productId + " at warehouse " + warehouseLocation
            );
        }

        int newQuantity = inventory.getQuantity() + quantityChange;
        
        if (newQuantity < 0) {
            return Result.failure("Insufficient inventory. Current: " + inventory.getQuantity() + 
                                ", Requested: " + Math.abs(quantityChange));
        }

        inventory.setQuantity(newQuantity);
        Inventory updated = inventoryRepository.updateInventory(inventory, connection);
        
        return Result.success(updated, "Inventory adjusted successfully");
    }
}
