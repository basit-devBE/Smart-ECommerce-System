package org.commerce.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.commerce.ECommerceApp;
import org.commerce.common.PerformanceMonitor;
import org.commerce.common.Result;
import org.commerce.daos.entities.Categories;
import org.commerce.daos.entities.Product;
import org.commerce.daos.entities.User;
import org.commerce.daos.entities.Review;

// import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Customer Product Listing Page
 */
public class ProductListingController {

    @FXML private Label welcomeLabel;
    @FXML private FlowPane productsContainer;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Label cacheStatsLabel;
    @FXML private Label performanceLabel;
    
    private ObservableList<Product> allProducts = FXCollections.observableArrayList();
    private ObservableList<Categories> categories = FXCollections.observableArrayList();
    private User currentUser;

    @FXML
    private void initialize() {
        currentUser = LoginController.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFirstname() + "!");
        }

        loadCategories();
        setupSortComboBox();
        setupFilters();
        loadProducts();
        updateCacheStats();
    }
    
    private void setupSortComboBox() {
        if (sortComboBox != null) {
            sortComboBox.getItems().addAll(
                "Name (A-Z)",
                "Name (Z-A)",
                "Price: Low to High",
                "Price: High to Low",
                "Newest First",
                "Oldest First"
            );
            sortComboBox.setValue("Name (A-Z)");
            sortComboBox.setOnAction(e -> loadProducts());
        }
    }

    private void loadCategories() {
        Result<List<Categories>> result = ECommerceApp.getCategoryService().getAllCategories();
        if (result.isSuccess()) {
            categories.addAll(result.getData());
            categoryFilter.getItems().add("All Categories");
            for (Categories cat : categories) {
                categoryFilter.getItems().add(cat.getCategoryName());
            }
            categoryFilter.setValue("All Categories");
        }
    }

    private void loadProducts() {
        productsContainer.getChildren().clear();
        allProducts.clear();
        
        // Measure performance
        long startTime = System.nanoTime();
        
        // Get sort option
        String sortOption = getSortOptionKey();
        
        Result<List<Product>> result = ECommerceApp.getProductService().getAllProductsSorted(sortOption);
        
        long duration = (System.nanoTime() - startTime) / 1_000_000; // Convert to ms
        
        if (result.isSuccess()) {
            allProducts.addAll(result.getData());
            displayProducts(allProducts);
            
            // Show performance info
            if (performanceLabel != null) {
                performanceLabel.setText(String.format("⚡ Loaded %d products in %dms (Cached: %s)", 
                    allProducts.size(), duration, duration < 10 ? "YES" : "NO"));
                performanceLabel.setStyle("-fx-text-fill: " + (duration < 10 ? "#10b981" : "#f59e0b") + ";");
            }
        } else {
            showAlert("Error", result.getMessage());
        }
        
        updateCacheStats();
    }
    
    private String getSortOptionKey() {
        if (sortComboBox == null || sortComboBox.getValue() == null) {
            return "name";
        }
        
        return switch (sortComboBox.getValue()) {
            case "Name (Z-A)" -> "name_desc";
            case "Price: Low to High" -> "price_asc";
            case "Price: High to Low" -> "price_desc";
            case "Newest First" -> "newest";
            case "Oldest First" -> "oldest";
            default -> "name";
        };
    }
    
    private void updateCacheStats() {
        if (cacheStatsLabel != null) {
            String stats = ECommerceApp.getProductService().getCacheStats();
            cacheStatsLabel.setText("📊 " + stats);
        }
    }

    private void displayProducts(List<Product> products) {
        productsContainer.getChildren().clear();
        
        for (Product product : products) {
            VBox productCard = createProductCard(product);
            productsContainer.getChildren().add(productCard);
        }
        
        if (products.isEmpty()) {
            Text noProducts = new Text("No products found");
            noProducts.setStyle("-fx-font-size: 18px; -fx-fill: #64748b;");
            productsContainer.getChildren().add(noProducts);
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPadding(new Insets(20));
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        
        // Product Name
        Label nameLabel = new Label(product.getProductName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        nameLabel.setWrapText(true);
        
        // Category
        Result<Categories> catResult = ECommerceApp.getCategoryService().getCategoryById(product.getCategoryId());
        String categoryName = catResult.isSuccess() ? catResult.getData().getCategoryName() : "Unknown";
        Label categoryLabel = new Label(categoryName);
        categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #667eea; -fx-background-color: #e0e7ff; " +
                              "-fx-padding: 4px 12px; -fx-background-radius: 12px; -fx-font-weight: 700;");
        
        // Description
        Label descLabel = new Label(product.getDescription());
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(60);
        
        // Price
        Label priceLabel = new Label("$" + product.getPrice().toString());
        priceLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        
        // Skip rating display on card to avoid MongoDB queries for every product
        // Users can click "View Details & Reviews" to see ratings
        
        // Stock
        Result<Integer> stockResult = ECommerceApp.getProductService().getTotalStock(product.getId());
        int stock = stockResult.isSuccess() ? stockResult.getData() : 0;
        Label stockLabel = new Label(stock > 0 ? "In Stock (" + stock + ")" : "Out of Stock");
        stockLabel.setStyle(stock > 0 
            ? "-fx-font-size: 13px; -fx-text-fill: #10b981; -fx-font-weight: 600;" 
            : "-fx-font-size: 13px; -fx-text-fill: #ef4444; -fx-font-weight: 600;");
        
        // Add to Cart Button
        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.getStyleClass().add("primary-button");
        addToCartBtn.setMaxWidth(Double.MAX_VALUE);
        addToCartBtn.setDisable(stock == 0);
        addToCartBtn.setOnAction(e -> handleAddToCart(product));
        
        // View Reviews Button
        Button viewReviewsBtn = new Button("View Details & Reviews");
        viewReviewsBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 14px; " +
                               "-fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-cursor: hand;");
        viewReviewsBtn.setMaxWidth(Double.MAX_VALUE);
        viewReviewsBtn.setOnAction(e -> showProductDetails(product));
        
        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        card.getChildren().addAll(nameLabel, categoryLabel, descLabel, spacer, priceLabel, stockLabel, addToCartBtn, viewReviewsBtn);
        
        return card;
    }
    
    private String getStarRating(double rating) {
        StringBuilder stars = new StringBuilder();
        int fullStars = (int) rating;
        boolean hasHalfStar = (rating - fullStars) >= 0.5;
        
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }
        if (hasHalfStar) {
            stars.append("⯨");
        }
        int emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
        for (int i = 0; i < emptyStars; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }
    
    private void showProductDetails(Product product) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Product Details & Reviews");
        dialog.setHeaderText(product.getProductName());
        
        // Create content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(600);
        
        // Product info
        Label priceLabel = new Label("Price: $" + product.getPrice());
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        
        Label descLabel = new Label(product.getDescription());
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");
        
        // Rating summary
        Result<Double> avgRatingResult = ECommerceApp.getReviewService().getAverageRating(product.getId());
        double avgRating = avgRatingResult.isSuccess() ? avgRatingResult.getData() : 0.0;
        
        Result<long[]> distributionResult = ECommerceApp.getReviewService().getRatingDistribution(product.getId());
        long[] distribution = distributionResult.isSuccess() ? distributionResult.getData() : new long[5];
        
        Label avgRatingLabel = new Label(String.format("Average Rating: %s %.1f/5.0", getStarRating(avgRating), avgRating));
        avgRatingLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Add separator
        Separator separator1 = new Separator();
        separator1.setPadding(new Insets(10, 0, 10, 0));
        
        // Write a Review Section
        Label writeReviewHeader = new Label("Write Your Review:");
        writeReviewHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Check if user is logged in
        VBox reviewFormBox = new VBox(10);
        if (currentUser == null) {
            Label loginMsg = new Label("Please log in to write a review.");
            loginMsg.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");
            reviewFormBox.getChildren().add(loginMsg);
        } else {
            // Rating selector
            HBox ratingBox = new HBox(10);
            ratingBox.setAlignment(Pos.CENTER_LEFT);
            Label ratingLabel = new Label("Rating:");
            ratingLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            ComboBox<Integer> ratingCombo = new ComboBox<>();
            ratingCombo.getItems().addAll(5, 4, 3, 2, 1);
            ratingCombo.setValue(5);
            ratingCombo.setPrefWidth(100);
            Label starsLabel = new Label("★ ★ ★ ★ ★");
            starsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f59e0b;");
            ratingBox.getChildren().addAll(ratingLabel, ratingCombo, starsLabel);
            
            // Title field
            Label titleLabel = new Label("Title:");
            titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            TextField titleField = new TextField();
            titleField.setPromptText("Summarize your review");
            titleField.setPrefWidth(550);
            
            // Comment area
            Label commentLabel = new Label("Comment:");
            commentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            TextArea commentArea = new TextArea();
            commentArea.setPromptText("Share your experience (minimum 10 characters)");
            commentArea.setPrefRowCount(4);
            commentArea.setPrefHeight(100);
            commentArea.setPrefWidth(550);
            commentArea.setWrapText(true);
            
            // Submit button
            Button submitBtn = new Button("Submit Review");
            submitBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 14px; " +
                              "-fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-cursor: hand;");
            submitBtn.setOnAction(e -> {
                String title = titleField.getText().trim();
                String comment = commentArea.getText().trim();
                int rating = ratingCombo.getValue();
                
                if (title.isEmpty()) {
                    showAlert("Validation Error", "Please enter a title for your review.");
                    return;
                }
                
                if (comment.length() < 10) {
                    showAlert("Validation Error", "Review comment must be at least 10 characters long.");
                    return;
                }
                
                // Create review
                try {
                    Review review = new Review();
                    review.setProductId(product.getId());
                    review.setUserId(currentUser.getId());
                    review.setUserName(currentUser.getFirstname() + " " + currentUser.getLastname());
                    review.setRating(rating);
                    review.setTitle(title);
                    review.setComment(comment);
                    
                    Result<Review> createResult = ECommerceApp.getReviewService().createReview(review);
                    
                    if (createResult.isSuccess()) {
                        // Log activity
                        ECommerceApp.getActivityLogService().logActivity(
                            currentUser.getId(),
                            currentUser.getFirstname() + " " + currentUser.getLastname(),
                            "WRITE_REVIEW",
                            "Product",
                            product.getId()
                        );
                        
                        showAlert("Success", "Thank you for your review!");
                        dialog.close();
                        
                        // Refresh the product listing
                        loadProducts();
                    } else {
                        showAlert("Error", "Failed to submit review: " + createResult.getMessage());
                    }
                } catch (Exception ex) {
                    showAlert("Error", "Failed to submit review: " + ex.getMessage());
                }
            });
            
            reviewFormBox.getChildren().addAll(
                ratingBox, 
                titleLabel, titleField, 
                commentLabel, commentArea, 
                submitBtn
            );
        }
        
        // Add separator
        Separator separator2 = new Separator();
        separator2.setPadding(new Insets(10, 0, 10, 0));
        
        // Reviews list (limit to most recent 20 to prevent memory issues)
        Label reviewsHeader = new Label("Customer Reviews (Most Recent 20):");
        reviewsHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10px 0;");
        
        ScrollPane reviewsScroll = new ScrollPane();
        reviewsScroll.setPrefHeight(250);
        reviewsScroll.setFitToWidth(true);
        
        VBox reviewsList = new VBox(10);
        Result<List<Review>> reviewsResult = ECommerceApp.getReviewService().getProductReviews(product.getId());
        List<Review> allReviews = reviewsResult.isSuccess() ? reviewsResult.getData() : List.of();
        
        // Limit to 20 most recent reviews to save memory
        List<Review> reviews = allReviews.stream().limit(20).toList();
        
        if (reviews.isEmpty()) {
            Label noReviews = new Label("No reviews yet. Be the first to review!");
            noReviews.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
            reviewsList.getChildren().add(noReviews);
        } else {
            for (Review review : reviews) {
                reviewsList.getChildren().add(createReviewCard(review));
            }
        }
        reviewsScroll.setContent(reviewsList);
        
        content.getChildren().addAll(
            priceLabel, descLabel, avgRatingLabel, 
            separator1, writeReviewHeader, reviewFormBox, 
            separator2, reviewsHeader, reviewsScroll
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefHeight(700);
        dialog.getDialogPane().setPrefWidth(650);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    private VBox createReviewCard(Review review) {
        VBox card = new VBox(8);
        card.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 8px; " +
                     "-fx-background-color: #f8fafc; -fx-padding: 15px; -fx-background-radius: 8px;");
        
        // Header with user and rating
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label userLabel = new Label(review.getUserName());
        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label starsLabel = new Label(getStarRating(review.getRating()));
        starsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f59e0b;");
        
        Label dateLabel = new Label(review.getCreatedAt().toLocalDate().toString());
        dateLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(userLabel, starsLabel, spacer, dateLabel);
        
        // Title
        Label titleLabel = new Label(review.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        // Comment
        Label commentLabel = new Label(review.getComment());
        commentLabel.setWrapText(true);
        commentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");
        
        // Verified badge
        if (review.isVerified()) {
            Label verifiedLabel = new Label("✓ Verified Purchase");
            verifiedLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-font-weight: 600;");
            card.getChildren().add(verifiedLabel);
        }
        
        card.getChildren().addAll(header, titleLabel, commentLabel);
        
        return card;
    }
    
    private void showWriteReviewDialog(Product product) {
        // Check if user is logged in
        if (currentUser == null) {
            showAlert("Error", "You must be logged in to write a review.");
            return;
        }
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Write a Review");
        dialog.setHeaderText("Review: " + product.getProductName());
        dialog.setResizable(true);
        
        // Use VBox for better layout control
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);
        content.setMinHeight(300);
        
        // Rating selector
        Label ratingLabel = new Label("Rating:");
        ratingLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        ComboBox<Integer> ratingCombo = new ComboBox<>();
        ratingCombo.getItems().addAll(5, 4, 3, 2, 1);
        ratingCombo.setValue(5);
        ratingCombo.setPrefWidth(100);
        
        HBox ratingBox = new HBox(10);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        Label starLabel = new Label("★ ★ ★ ★ ★");
        starLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f59e0b;");
        ratingBox.getChildren().addAll(ratingLabel, ratingCombo, starLabel);
        
        // Title
        Label titleLabel = new Label("Title:");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField titleField = new TextField();
        titleField.setPromptText("Summarize your review");
        titleField.setPrefWidth(460);
        
        // Comment
        Label commentLabel = new Label("Review:");
        commentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Share your experience with this product (minimum 10 characters)");
        commentArea.setPrefRowCount(6);
        commentArea.setPrefHeight(150);
        commentArea.setPrefWidth(460);
        commentArea.setWrapText(true);
        
        content.getChildren().addAll(ratingBox, titleLabel, titleField, commentLabel, commentArea);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setMinHeight(450);
        dialog.getDialogPane().setPrefHeight(500);
        dialog.getDialogPane().setPrefWidth(550);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Force layout
        dialog.getDialogPane().layout();
        
        System.out.println("Opening review dialog for: " + product.getProductName());
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String title = titleField.getText().trim();
            String comment = commentArea.getText().trim();
            int rating = ratingCombo.getValue();
            
            if (title.isEmpty()) {
                showAlert("Validation Error", "Please enter a title for your review.");
                return;
            }
            
            if (comment.length() < 10) {
                showAlert("Validation Error", "Review comment must be at least 10 characters long.");
                return;
            }
            
            // Create review
            try {
                Review review = new Review();
                review.setProductId(product.getId());
                review.setUserId(currentUser.getId());
                review.setUserName(currentUser.getFirstname() + " " + currentUser.getLastname());
                review.setRating(rating);
                review.setTitle(title);
                review.setComment(comment);
                
                Result<Review> createResult = ECommerceApp.getReviewService().createReview(review);
                
                if (createResult.isSuccess()) {
                    // Log activity
                    ECommerceApp.getActivityLogService().logActivity(
                        currentUser.getId(),
                        currentUser.getFirstname() + " " + currentUser.getLastname(),
                        "WRITE_REVIEW",
                        "Product",
                        product.getId()
                    );
                    
                    showAlert("Success", "Thank you for your review!");
                    
                    // Refresh products to show updated rating
                    loadProducts();
                } else {
                    showAlert("Error", "Failed to submit review: " + createResult.getMessage());
                }
                
            } catch (Exception e) {
                showAlert("Error", "Failed to submit review: " + e.getMessage());
            }
        }
    }

    private void setupFilters() {
        // Category filter
        categoryFilter.setOnAction(e -> filterProducts());
        
        // Search filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterProducts());
    }

    private void filterProducts() {
        String selectedCategory = categoryFilter.getValue();
        String searchText = searchField.getText().trim();
        
        // Determine category ID for database query
        Integer categoryId = null;
        if (!selectedCategory.equals("All Categories")) {
            for (Categories cat : categories) {
                if (cat.getCategoryName().equals(selectedCategory)) {
                    categoryId = cat.getId();
                    break;
                }
            }
        }
        
        // Use optimized database search instead of in-memory filtering
        // This leverages PostgreSQL indexes for better performance
        long startTime = PerformanceMonitor.startTiming("Product Search");
        
        Result<List<Product>> result = ECommerceApp.getProductService()
            .searchProductsByCategory(categoryId, searchText.isEmpty() ? null : searchText);
        
        long duration = PerformanceMonitor.endTiming("Product Search", startTime);
        
        if (result.isSuccess()) {
            allProducts.clear();
            allProducts.addAll(result.getData());
            displayProducts(allProducts);
            
            // Show performance info in console (can be viewed in logs)
            System.out.printf("[SEARCH] Found %d products in %d ms (Category: %s, Search: '%s')%n",
                allProducts.size(), duration, selectedCategory, searchText);
        } else {
            showAlert("Error", "Search failed: " + result.getMessage());
        }
    }

    @FXML
    private void handleAddToCart(Product product) {
        // TODO: Implement cart functionality
        showAlert("Coming Soon", "Cart functionality will be implemented soon!\nProduct: " + product.getProductName());
    }

    @FXML
    private void handleRefresh() {
        // Clear cache and reload to show difference
        ECommerceApp.getProductService().invalidateAllCaches();
        updateCacheStats();
        loadProducts();
    }
    
    @FXML
    private void handleClearCache() {
        ECommerceApp.getProductService().invalidateAllCaches();
        ECommerceApp.getCategoryService().invalidateAllCaches();
        updateCacheStats();
        showAlert("Success", "All caches cleared! Next queries will hit database.");
    }
    
    @FXML
    private void handleShowCacheStats() {
        String productStats = ECommerceApp.getProductService().getCacheStats();
        String categoryStats = ECommerceApp.getCategoryService().getCacheStats();
        String userStats = ECommerceApp.getUserService().getCacheStats();
        
        showAlert("Cache Statistics", 
            "Product Service:\n  " + productStats + "\n\n" +
            "Category Service:\n  " + categoryStats + "\n\n" +
            "User Service:\n  " + userStats);
    }

    @FXML
    private void handleLogout() {
        try {
            ECommerceApp.showLoginView();
        } catch (Exception e) {
            showAlert("Error", "Failed to logout: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
