package org.commerce.daos.repositories;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.commerce.config.MongoDBConfig;
import org.commerce.daos.entities.Review;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

/**
 * Repository for Review operations in MongoDB
 */
public class ReviewRepository {
    private final MongoCollection<Document> collection;
    
    public ReviewRepository() {
        if (MongoDBConfig.isConnected()) {
            this.collection = MongoDBConfig.getDatabase().getCollection("reviews");
            createIndexes();
        } else {
            this.collection = null;
        }
    }
    
    private void createIndexes() {
        if (collection != null) {
            collection.createIndex(new Document("productId", 1));
            collection.createIndex(new Document("userId", 1));
            collection.createIndex(new Document("createdAt", -1));
            collection.createIndex(new Document("rating", -1));
        }
    }
    
    /**
     * Creates a new review
     */
    public Review create(Review review) {
        if (collection == null) return null;
        
        Document doc = new Document()
            .append("productId", review.getProductId())
            .append("userId", review.getUserId())
            .append("userName", review.getUserName())
            .append("rating", review.getRating())
            .append("title", review.getTitle())
            .append("comment", review.getComment())
            .append("verified", review.isVerified())
            .append("helpfulCount", review.getHelpfulCount())
            .append("createdAt", review.getCreatedAt())
            .append("updatedAt", review.getUpdatedAt());
        
        if (review.getImages() != null) {
            doc.append("images", review.getImages());
        }
        
        collection.insertOne(doc);
        review.setId(doc.getObjectId("_id"));
        return review;
    }
    
    /**
     * Gets a review by ID
     */
    public Review findById(String id) {
        if (collection == null) return null;
        
        Document doc = collection.find(eq("_id", new ObjectId(id))).first();
        return doc != null ? documentToReview(doc) : null;
    }
    
    /**
     * Gets all reviews for a product
     */
    public List<Review> findByProductId(int productId) {
        if (collection == null) return new ArrayList<>();
        
        List<Review> reviews = new ArrayList<>();
        collection.find(eq("productId", productId))
            .sort(Sorts.descending("createdAt"))
            .forEach(doc -> reviews.add(documentToReview(doc)));
        return reviews;
    }
    
    /**
     * Gets reviews by user
     */
    public List<Review> findByUserId(int userId) {
        if (collection == null) return new ArrayList<>();
        
        List<Review> reviews = new ArrayList<>();
        collection.find(eq("userId", userId))
            .sort(Sorts.descending("createdAt"))
            .forEach(doc -> reviews.add(documentToReview(doc)));
        return reviews;
    }
    
    /**
     * Gets average rating for a product
     */
    public double getAverageRating(int productId) {
        List<Review> reviews = findByProductId(productId);
        if (reviews.isEmpty()) return 0.0;
        
        double sum = reviews.stream().mapToInt(Review::getRating).sum();
        return sum / reviews.size();
    }
    
    /**
     * Gets review count by rating
     */
    public long getCountByRating(int productId, int rating) {
        if (collection == null) return 0;
        
        return collection.countDocuments(and(
            eq("productId", productId),
            eq("rating", rating)
        ));
    }
    
    /**
     * Updates a review
     */
    public boolean update(String id, Review review) {
        if (collection == null) return false;
        
        Document update = new Document()
            .append("rating", review.getRating())
            .append("title", review.getTitle())
            .append("comment", review.getComment())
            .append("updatedAt", LocalDateTime.now());
        
        return collection.updateOne(
            eq("_id", new ObjectId(id)),
            new Document("$set", update)
        ).getModifiedCount() > 0;
    }
    
    /**
     * Increments helpful count
     */
    public boolean incrementHelpful(String id) {
        if (collection == null) return false;
        
        return collection.updateOne(
            eq("_id", new ObjectId(id)),
            Updates.combine(
                Updates.inc("helpfulCount", 1),
                Updates.set("updatedAt", LocalDateTime.now())
            )
        ).getModifiedCount() > 0;
    }
    
    /**
     * Deletes a review
     */
    public boolean delete(String id) {
        if (collection == null) return false;
        
        return collection.deleteOne(eq("_id", new ObjectId(id)))
            .getDeletedCount() > 0;
    }
    
    /**
     * Gets recent reviews
     */
    public List<Review> getRecentReviews(int limit) {
        if (collection == null) return new ArrayList<>();
        
        List<Review> reviews = new ArrayList<>();
        collection.find()
            .sort(Sorts.descending("createdAt"))
            .limit(limit)
            .forEach(doc -> reviews.add(documentToReview(doc)));
        return reviews;
    }
    
    /**
     * Converts MongoDB Document to Review entity
     */
    @SuppressWarnings("unchecked")
    private Review documentToReview(Document doc) {
        Review review = new Review();
        review.setId(doc.getObjectId("_id"));
        review.setProductId(doc.getInteger("productId"));
        review.setUserId(doc.getInteger("userId"));
        review.setUserName(doc.getString("userName"));
        review.setRating(doc.getInteger("rating"));
        review.setTitle(doc.getString("title"));
        review.setComment(doc.getString("comment"));
        review.setVerified(doc.getBoolean("verified", false));
        review.setHelpfulCount(doc.getInteger("helpfulCount", 0));
        
        if (doc.get("images") != null) {
            review.setImages((List<String>) doc.get("images"));
        }
        
        if (doc.get("createdAt") != null) {
            review.setCreatedAt((LocalDateTime) doc.get("createdAt"));
        }
        if (doc.get("updatedAt") != null) {
            review.setUpdatedAt((LocalDateTime) doc.get("updatedAt"));
        }
        
        return review;
    }
}
