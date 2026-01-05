package org.commerce.services;

import org.commerce.common.Result;
import org.commerce.common.ValidationResult;
import org.commerce.entities.Review;
import org.commerce.repositories.ReviewRepository;

import java.util.List;

/**
 * Service for Review operations
 */
public class ReviewService {
    private final ReviewRepository reviewRepository;
    
    public ReviewService() {
        this.reviewRepository = new ReviewRepository();
    }
    
    /**
     * Creates a new review
     */
    public Result<Review> createReview(Review review) {
        ValidationResult validation = validateReview(review);
        if (!validation.isValid()) {
            return Result.failure(validation.getErrorMessage());
        }
        
        try {
            Review created = reviewRepository.create(review);
            if (created != null) {
                return Result.success(created, "Review created successfully");
            }
            return Result.failure("MongoDB not connected");
        } catch (Exception e) {
            return Result.failure("Failed to create review: " + e.getMessage());
        }
    }
    
    /**
     * Gets all reviews for a product
     */
    public Result<List<Review>> getProductReviews(int productId) {
        if (productId <= 0) {
            return Result.failure("Invalid product ID");
        }
        
        try {
            List<Review> reviews = reviewRepository.findByProductId(productId);
            return Result.success(reviews);
        } catch (Exception e) {
            return Result.failure("Failed to fetch reviews: " + e.getMessage());
        }
    }
    
    /**
     * Gets user's reviews
     */
    public Result<List<Review>> getUserReviews(int userId) {
        if (userId <= 0) {
            return Result.failure("Invalid user ID");
        }
        
        try {
            List<Review> reviews = reviewRepository.findByUserId(userId);
            return Result.success(reviews);
        } catch (Exception e) {
            return Result.failure("Failed to fetch user reviews: " + e.getMessage());
        }
    }
    
    /**
     * Gets average rating for a product
     */
    public Result<Double> getAverageRating(int productId) {
        if (productId <= 0) {
            return Result.failure("Invalid product ID");
        }
        
        try {
            double avgRating = reviewRepository.getAverageRating(productId);
            return Result.success(avgRating);
        } catch (Exception e) {
            return Result.failure("Failed to calculate average rating: " + e.getMessage());
        }
    }
    
    /**
     * Gets rating distribution for a product
     */
    public Result<long[]> getRatingDistribution(int productId) {
        if (productId <= 0) {
            return Result.failure("Invalid product ID");
        }
        
        try {
            long[] distribution = new long[5];
            for (int i = 1; i <= 5; i++) {
                distribution[i-1] = reviewRepository.getCountByRating(productId, i);
            }
            return Result.success(distribution);
        } catch (Exception e) {
            return Result.failure("Failed to get rating distribution: " + e.getMessage());
        }
    }
    
    /**
     * Updates a review
     */
    public Result<Boolean> updateReview(String reviewId, Review review) {
        if (reviewId == null || reviewId.isEmpty()) {
            return Result.failure("Invalid review ID");
        }
        
        ValidationResult validation = validateReview(review);
        if (!validation.isValid()) {
            return Result.failure(validation.getErrorMessage());
        }
        
        try {
            boolean updated = reviewRepository.update(reviewId, review);
            if (updated) {
                return Result.success(true, "Review updated successfully");
            }
            return Result.failure("Review not found");
        } catch (Exception e) {
            return Result.failure("Failed to update review: " + e.getMessage());
        }
    }
    
    /**
     * Marks review as helpful
     */
    public Result<Boolean> markHelpful(String reviewId) {
        if (reviewId == null || reviewId.isEmpty()) {
            return Result.failure("Invalid review ID");
        }
        
        try {
            boolean updated = reviewRepository.incrementHelpful(reviewId);
            if (updated) {
                return Result.success(true, "Review marked as helpful");
            }
            return Result.failure("Review not found");
        } catch (Exception e) {
            return Result.failure("Failed to mark review as helpful: " + e.getMessage());
        }
    }
    
    /**
     * Deletes a review
     */
    public Result<Boolean> deleteReview(String reviewId) {
        if (reviewId == null || reviewId.isEmpty()) {
            return Result.failure("Invalid review ID");
        }
        
        try {
            boolean deleted = reviewRepository.delete(reviewId);
            if (deleted) {
                return Result.success(true, "Review deleted successfully");
            }
            return Result.failure("Review not found");
        } catch (Exception e) {
            return Result.failure("Failed to delete review: " + e.getMessage());
        }
    }
    
    /**
     * Gets recent reviews
     */
    public Result<List<Review>> getRecentReviews(int limit) {
        try {
            List<Review> reviews = reviewRepository.getRecentReviews(limit);
            return Result.success(reviews);
        } catch (Exception e) {
            return Result.failure("Failed to fetch recent reviews: " + e.getMessage());
        }
    }
    
    /**
     * Validates review data
     */
    private ValidationResult validateReview(Review review) {
        ValidationResult result = new ValidationResult();
        
        if (review.getProductId() <= 0) {
            result.addError("Invalid product ID");
        }
        
        if (review.getUserId() <= 0) {
            result.addError("Invalid user ID");
        }
        
        if (review.getRating() < 1 || review.getRating() > 5) {
            result.addError("Rating must be between 1 and 5");
        }
        
        if (review.getTitle() == null || review.getTitle().trim().isEmpty()) {
            result.addError("Review title is required");
        }
        
        if (review.getComment() == null || review.getComment().trim().isEmpty()) {
            result.addError("Review comment is required");
        }
        
        if (review.getComment() != null && review.getComment().length() < 10) {
            result.addError("Review comment must be at least 10 characters");
        }
        
        return result;
    }
}
