package org.commerce.daos.entities;

import java.time.LocalDateTime;

public class Inventory {
    private int id;
    private int productId;
    private int quantity;
    private String warehouseLocation;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;

    public Inventory() {}

    public Inventory(int id, int productId, int quantity, String warehouseLocation, LocalDateTime createdAt, LocalDateTime lastUpdated) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.warehouseLocation = warehouseLocation;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getWarehouseLocation() { return warehouseLocation; }
    public void setWarehouseLocation(String warehouseLocation) { this.warehouseLocation = warehouseLocation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
