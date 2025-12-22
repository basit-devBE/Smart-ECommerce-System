package org.commerce.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Orders {
    private int id;
    private int userId;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal totalAmount;

    public Orders() {}

    public Orders(int id, int userId, LocalDateTime orderDate, String status, BigDecimal totalAmount) {
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}

