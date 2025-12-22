package org.commerce.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.commerce.entities.Inventory;

public class InventoryRepository {
    
    public Inventory createInventory(Inventory inventory, Connection connection){
        String SQL = "INSERT INTO inventory (product_id, quantity, warehouse_location) VALUES (?, ?, ?) RETURNING *";
        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            pstmt.setInt(1, inventory.getProductId());
            pstmt.setInt(2, inventory.getQuantity());
            pstmt.setString(3, inventory.getWarehouseLocation());
            
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                inventory.setId(rs.getInt("id"));
                inventory.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                inventory.setLastUpdated(rs.getTimestamp("last_updated").toLocalDateTime());
                System.out.println("Inventory created successfully.");
                return inventory;
            }
        }catch(SQLException e){
            System.err.println("Failed to create inventory: " + e.getMessage());
        }
        return null;
    }
    
    public Inventory updateInventory(Inventory inventory, Connection connection){
        String SQL = "UPDATE inventory SET quantity = ?, warehouse_location = ?, last_updated = CURRENT_TIMESTAMP WHERE id = ? RETURNING *";
        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            pstmt.setInt(1, inventory.getQuantity());
            pstmt.setString(2, inventory.getWarehouseLocation());
            pstmt.setInt(3, inventory.getId());
            
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                inventory.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                inventory.setLastUpdated(rs.getTimestamp("last_updated").toLocalDateTime());
                System.out.println("Inventory updated successfully.");
                return inventory;
            }
        }catch(SQLException e){
            System.err.println("Failed to update inventory: " + e.getMessage());
        }
        return null;
    }
    
    public boolean deleteInventory(int inventoryId, Connection connection){
        String SQL = "DELETE FROM inventory WHERE id = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            pstmt.setInt(1, inventoryId);
            int rowsAffected = pstmt.executeUpdate();
            if(rowsAffected > 0){
                System.out.println("Inventory deleted successfully.");
                return true;
            }
        }catch(SQLException e){
            System.err.println("Failed to delete inventory: " + e.getMessage());
        }
        return false;
    }
    
    public List<Inventory> getInventoryByProductId(int productId, Connection connection){
        String SQL = "SELECT * FROM inventory WHERE product_id = ?";
        List<Inventory> inventories = new ArrayList<>();
        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                Inventory inventory = new Inventory();
                inventory.setId(rs.getInt("id"));
                inventory.setProductId(rs.getInt("product_id"));
                inventory.setQuantity(rs.getInt("quantity"));
                inventory.setWarehouseLocation(rs.getString("warehouse_location"));
                inventory.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                inventory.setLastUpdated(rs.getTimestamp("last_updated").toLocalDateTime());
                inventories.add(inventory);
            }
        }catch(SQLException e){
            System.err.println("Failed to retrieve inventory: " + e.getMessage());
        }
        return inventories;
    }
    
    public Inventory getInventoryByProductAndWarehouse(int productId, String warehouseLocation, Connection connection){
        String SQL = "SELECT * FROM inventory WHERE product_id = ? AND warehouse_location = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(SQL)){
            pstmt.setInt(1, productId);
            pstmt.setString(2, warehouseLocation);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                Inventory inventory = new Inventory();
                inventory.setId(rs.getInt("id"));
                inventory.setProductId(rs.getInt("product_id"));
                inventory.setQuantity(rs.getInt("quantity"));
                inventory.setWarehouseLocation(rs.getString("warehouse_location"));
                inventory.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                inventory.setLastUpdated(rs.getTimestamp("last_updated").toLocalDateTime());
                return inventory;
            }
        }catch(SQLException e){
            System.err.println("Failed to retrieve inventory: " + e.getMessage());
        }
        return null;
    }
}
