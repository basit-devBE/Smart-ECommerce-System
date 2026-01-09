package org.commerce.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.commerce.ECommerceApp;
import org.commerce.daos.entities.Categories;
import org.commerce.daos.entities.User;
import org.commerce.controllers.ProductManagementController.ProductDisplay;
import org.commerce.controllers.InventoryManagementController.InventoryDisplay;

/**
 * Main Dashboard Controller that delegates to specialized controllers.
 * Follows Single Responsibility Principle and Delegation Pattern.
 * Responsibilities: Initialize UI, coordinate between specialized controllers, handle top-level actions.
 */
public class DashboardController extends BaseController {

    @FXML private Label welcomeLabel;
    @FXML private Label userRoleLabel;
    
    // Products Tab
    @FXML private TableView<ProductDisplay> productsTable;
    @FXML private TableColumn<ProductDisplay, Integer> productIdCol;
    @FXML private TableColumn<ProductDisplay, String> productNameCol;
    @FXML private TableColumn<ProductDisplay, String> productPriceCol;
    @FXML private TableColumn<ProductDisplay, Integer> productStockCol;
    @FXML private TableColumn<ProductDisplay, String> productCategoryCol;
    
    // Categories Tab
    @FXML private TableView<Categories> categoriesTable;
    @FXML private TableColumn<Categories, Integer> categoryIdCol;
    @FXML private TableColumn<Categories, String> categoryNameCol;
    @FXML private TableColumn<Categories, String> categoryDescCol;
    
    // Inventory Tab
    @FXML private TableView<InventoryDisplay> inventoryTable;
    @FXML private TableColumn<InventoryDisplay, Integer> inventoryIdCol;
    @FXML private TableColumn<InventoryDisplay, String> inventoryProductCol;
    @FXML private TableColumn<InventoryDisplay, Integer> inventoryQuantityCol;
    @FXML private TableColumn<InventoryDisplay, String> inventoryWarehouseCol;
    
    // Users Tab
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> userIdCol;
    @FXML private TableColumn<User, String> userNameCol;
    @FXML private TableColumn<User, String> userEmailCol;
    @FXML private TableColumn<User, String> userRoleCol;

    // Observable lists for table data
    private final ObservableList<ProductDisplay> productsList = FXCollections.observableArrayList();
    private final ObservableList<Categories> categoriesList = FXCollections.observableArrayList();
    private final ObservableList<InventoryDisplay> inventoryList = FXCollections.observableArrayList();
    private final ObservableList<User> usersList = FXCollections.observableArrayList();
    
    // Specialized controllers for each domain
    private ProductManagementController productController;
    private CategoryManagementController categoryController;
    private InventoryManagementController inventoryController;
    private UserManagementController userController;

    @FXML
    private void initialize() {
        User currentUser = LoginController.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFirstname() + " " + currentUser.getLastname());
            userRoleLabel.setText(currentUser.getUserRole().toString());
        }

        // Initialize specialized controllers
        productController = new ProductManagementController(productsList, categoriesList);
        categoryController = new CategoryManagementController(categoriesList);
        inventoryController = new InventoryManagementController(inventoryList);
        userController = new UserManagementController(usersList);

        setupProductsTable();
        setupCategoriesTable();
        setupInventoryTable();
        setupUsersTable();
        
        loadAllData();
    }

    private void setupProductsTable() {
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        productPriceCol.setCellValueFactory(new PropertyValueFactory<>("priceFormatted"));
        productStockCol.setCellValueFactory(new PropertyValueFactory<>("totalStock"));
        productCategoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        
        productsTable.setItems(productsList);
    }

    private void setupCategoriesTable() {
        categoryIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        categoryNameCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        categoryDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        categoriesTable.setItems(categoriesList);
    }

    private void setupInventoryTable() {
        inventoryIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        inventoryProductCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        inventoryQuantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        inventoryWarehouseCol.setCellValueFactory(new PropertyValueFactory<>("warehouseLocation"));
        
        inventoryTable.setItems(inventoryList);
    }

    private void setupUsersTable() {
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        userNameCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFirstname() + " " + cellData.getValue().getLastname()
            )
        );
        userEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        userRoleCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getUserRole().toString()
            )
        );
        
        usersTable.setItems(usersList);
    }

    private void loadAllData() {
        productController.loadProducts();
        categoryController.loadCategories();
        inventoryController.loadInventory();
        userController.loadUsers();
    }

    // ================== Product Actions ==================
    
    @FXML
    private void handleAddProduct() {
        productController.handleAdd();
    }

    @FXML
    private void handleEditProduct() {
        ProductDisplay selected = productsTable.getSelectionModel().getSelectedItem();
        productController.handleEdit(selected);
    }

    @FXML
    private void handleDeleteProduct() {
        ProductDisplay selected = productsTable.getSelectionModel().getSelectedItem();
        productController.handleDelete(selected);
    }

    // ================== Category Actions ==================
    
    @FXML
    private void handleAddCategory() {
        categoryController.handleAdd();
    }

    @FXML
    private void handleEditCategory() {
        Categories selected = categoriesTable.getSelectionModel().getSelectedItem();
        categoryController.handleEdit(selected);
    }

    @FXML
    private void handleDeleteCategory() {
        Categories selected = categoriesTable.getSelectionModel().getSelectedItem();
        categoryController.handleDelete(selected, () -> productController.loadProducts());
    }

    // ================== Inventory Actions ==================
    
    @FXML
    private void handleAddInventory() {
        inventoryController.handleAdd(() -> productController.loadProducts());
    }

    @FXML
    private void handleAdjustInventory() {
        InventoryDisplay selected = inventoryTable.getSelectionModel().getSelectedItem();
        inventoryController.handleAdjust(selected, () -> productController.loadProducts());
    }

    // ================== User Actions ==================
    
    @FXML
    private void handleAddUser() {
        userController.handleAdd();
    }

    // ================== Dashboard Actions ==================
    
    @FXML
    private void handleRefresh() {
        long startTime = System.nanoTime();
        loadAllData();
        long duration = (System.nanoTime() - startTime) / 1_000_000;
        
        showAlert("Refreshed", String.format("Data refreshed successfully in %dms!\n\n%s", 
            duration,
            getCacheStatsMessage()));
    }
    
    private String getCacheStatsMessage() {
        return "Cache Statistics:\n" +
               "• " + ECommerceApp.getProductService().getCacheStats() + "\n" +
               "• " + ECommerceApp.getCategoryService().getCacheStats() + "\n" +
               "• " + ECommerceApp.getUserService().getCacheStats();
    }
    
    @FXML
    private void handleShowCacheStats() {
        String stats = "📊 Cache Performance Statistics\n\n" +
                      "Product Service:\n  " + ECommerceApp.getProductService().getCacheStats() + "\n\n" +
                      "Category Service:\n  " + ECommerceApp.getCategoryService().getCacheStats() + "\n\n" +
                      "User Service:\n  " + ECommerceApp.getUserService().getCacheStats() + "\n\n" +
                      "💡 Tip: Click 'Clear Cache' then 'Refresh' to see database query time vs cache hit time.";
        
        showAlert("Cache Statistics", stats);
    }
    
    @FXML
    private void handleClearCache() {
        ECommerceApp.getProductService().invalidateAllCaches();
        ECommerceApp.getCategoryService().invalidateAllCaches();
        ECommerceApp.getUserService().invalidateAllCaches();
        showAlert("Success", "All caches cleared!\n\nNext data load will query the database directly.\nClick 'Refresh' to see the difference in performance.");
    }

    @FXML
    private void handleLogout() {
        try {
            ECommerceApp.showLoginView();
        } catch (Exception e) {
            showError("Error", "Failed to logout: " + e.getMessage());
        }
    }
    
    // ================== Table Setup Methods ==================
    
    // Note: Table setup methods are defined earlier in the class
}