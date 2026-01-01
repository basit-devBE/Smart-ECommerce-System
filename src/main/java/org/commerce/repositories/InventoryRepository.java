package org.commerce.repositories;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.commerce.entities.Inventory;
import org.commerce.repositories.interfaces.IInventoryRepository;

/**
 * Repository implementation for Inventory entity operations.
 * Uses BaseRepository to eliminate code duplication.
 */
public class InventoryRepository extends BaseRepository implements IInventoryRepository {
    
    @Override
    public Inventory createInventory(Inventory inventory, Connection connection) {
        String SQL = "INSERT INTO inventory (product_id, quantity, warehouse_location) " +
                     "VALUES (?, ?, ?) RETURNING *";
        
        return executeInsertReturning(
            connection,
            SQL,
            this::mapInventory,
            inventory.getProductId(),
            inventory.getQuantity(),
            inventory.getWarehouseLocation()
        );
    }
    
    @Override
    public Inventory updateInventory(Inventory inventory, Connection connection) {
        String SQL = "UPDATE inventory SET quantity = ?, warehouse_location = ?, " +
                     "last_updated = CURRENT_TIMESTAMP WHERE id = ? RETURNING *";
        
        return executeInsertReturning(
            connection,
            SQL,
            this::mapInventory,
            inventory.getQuantity(),
            inventory.getWarehouseLocation(),
            inventory.getId()
        );
    }
    
    @Override
    public boolean deleteInventory(int inventoryId, Connection connection) {
        String SQL = "DELETE FROM inventory WHERE id = ?";
        return executeUpdate(connection, SQL, inventoryId) > 0;
    }
    
    @Override
    public List<Inventory> getInventoryByProductId(int productId, Connection connection) {
        String SQL = "SELECT * FROM inventory WHERE product_id = ?";
        return executeQueryList(connection, SQL, this::mapInventory, productId);
    }
    
    @Override
    public Inventory getInventoryByProductAndWarehouse(int productId, String warehouseLocation, Connection connection) {
        String SQL = "SELECT * FROM inventory WHERE product_id = ? AND warehouse_location = ?";
        return executeQuerySingle(connection, SQL, this::mapInventory, productId, warehouseLocation);
    }
    
    /**
     * Maps a ResultSet row to an Inventory entity.
     */
    private Inventory mapInventory(ResultSet rs) throws SQLException {
        Inventory inventory = new Inventory();
        inventory.setId(rs.getInt("id"));
        inventory.setProductId(rs.getInt("product_id"));
        inventory.setQuantity(rs.getInt("quantity"));
        inventory.setWarehouseLocation(rs.getString("warehouse_location"));
        inventory.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        inventory.setLastUpdated(rs.getTimestamp("last_updated").toLocalDateTime());
        return inventory;
    }
}
