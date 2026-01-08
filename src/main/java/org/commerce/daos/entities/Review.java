package org.commerce.daos.entities;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Review entity for MongoDB storage
 */
public class Review {
    private ObjectId id;
    private int productId;
    private int userId;
    private String userName;
    private int rating; // 1-5 stars
    private String title;
    private String comment;
    private List<String> images; // Optional review images
    private boolean verified; // Verified purchase
    private int helpfulCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public Review() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.helpfulCount = 0;
    }
    
    public Review(int productId, int userId, String userName, int rating, String title, String comment) {
        this();
        this.productId = productId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.title = title;
        this.comment = comment;
    }
    
    // Getters and Setters
    public ObjectId getId() {
        return id;
    }
    
    public void setId(ObjectId id) {
        this.id = id;
    }
    
    public String getIdString() {
        return id != null ? id.toHexString() : null;
    }
    
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
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
    
    public int getRating() {
        return rating;
    }
    
    public void setRating(int rating) {
        this.rating = rating;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public List<String> getImages() {
        return images;
    }
    
    public void setImages(List<String> images) {
        this.images = images;
    }
    
    public boolean isVerified() {
        return verified;
    }
    
    public void setVerified(boolean verified) {
        this.verified = verified;
    }
    
    public int getHelpfulCount() {
        return helpfulCount;
    }
    
    public void setHelpfulCount(int helpfulCount) {
        this.helpfulCount = helpfulCount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
