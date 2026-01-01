package org.commerce.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.commerce.ECommerceApp;
import org.commerce.common.PerformanceMonitor;
import org.commerce.common.Result;
import org.commerce.entities.Categories;
import org.commerce.entities.Product;
import org.commerce.entities.User;

// import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for Customer Product Listing Page
 */
public class ProductListingController {

    @FXML private Label welcomeLabel;
    @FXML private FlowPane productsContainer;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private TextField searchField;
    
    private ObservableList<Product> allProducts = FXCollections.observableArrayList();
    private ObservableList<Categories> categories = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        User currentUser = LoginController.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFirstname() + "!");
        }

        loadCategories();
        loadProducts();
        setupFilters();
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
        
        Result<List<Product>> result = ECommerceApp.getProductService().getAllProducts();
        
        if (result.isSuccess()) {
            allProducts.addAll(result.getData());
            displayProducts(allProducts);
        } else {
            showAlert("Error", "Failed to load products: " + result.getMessage());
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
        
        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        card.getChildren().addAll(nameLabel, categoryLabel, descLabel, spacer, priceLabel, stockLabel, addToCartBtn);
        
        return card;
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
        loadProducts();
        showAlert("Refreshed", "Product list has been refreshed!");
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
