package org.commerce.entities;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity Log entity for MongoDB storage
 */
public class ActivityLog {
    private ObjectId id;
    private int userId;
    private String userName;
    private String action; // LOGIN, LOGOUT, VIEW_PRODUCT, ADD_TO_CART, PURCHASE, etc.
    private String entityType; // USER, PRODUCT, ORDER, etc.
    private Integer entityId;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;
    
    public ActivityLog() {
        this.timestamp = LocalDateTime.now();
        this.metadata = new HashMap<>();
    }
    
    public ActivityLog(int userId, String userName, String action) {
        this();
        this.userId = userId;
        this.userName = userName;
        this.action = action;
    }
    
    // Getters and Setters
    public ObjectId getId() {
        return id;
    }
    
    public void setId(ObjectId id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public Integer getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
