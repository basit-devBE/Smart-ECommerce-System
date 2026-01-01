package org.commerce.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.commerce.ECommerceApp;
import org.commerce.common.Result;
import org.commerce.entities.Categories;
import org.commerce.entities.Inventory;
import org.commerce.entities.Product;
import org.commerce.entities.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the main Dashboard
 */
public class DashboardController {

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
    
    // Users Tab
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> userIdCol;
    @FXML private TableColumn<User, String> userNameCol;
    @FXML private TableColumn<User, String> userEmailCol;
    @FXML private TableColumn<User, String> userRoleCol;

    private ObservableList<ProductDisplay> productsList = FXCollections.observableArrayList();
    private ObservableList<Categories> categoriesList = FXCollections.observableArrayList();
    private ObservableList<User> usersList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        User currentUser = LoginController.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFirstname() + " " + currentUser.getLastname());
            userRoleLabel.setText(currentUser.getUserRole().toString());
        }

        setupProductsTable();
        setupCategoriesTable();
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
        loadProducts();
        loadCategories();
        loadUsers();
    }

    private void loadProducts() {
        productsList.clear();
        Result<List<Product>> result = ECommerceApp.getProductService().getAllProducts();
        
        if (result.isSuccess()) {
            for (Product product : result.getData()) {
                Result<Integer> stockResult = ECommerceApp.getProductService().getTotalStock(product.getId());
                int totalStock = stockResult.isSuccess() ? stockResult.getData() : 0;
                
                // Get category name
                Result<Categories> catResult = ECommerceApp.getCategoryService().getCategoryById(product.getCategoryId());
                String categoryName = catResult.isSuccess() ? catResult.getData().getCategoryName() : "Unknown";
                
                productsList.add(new ProductDisplay(
                    product.getId(),
                    product.getProductName(),
                    product.getPrice(),
                    totalStock,
                    categoryName
                ));
            }
        }
    }

    private void loadCategories() {
        categoriesList.clear();
        Result<List<Categories>> result = ECommerceApp.getCategoryService().getAllCategories();
        
        if (result.isSuccess()) {
            categoriesList.addAll(result.getData());
        }
    }

    private void loadUsers() {
        usersList.clear();
        Result<List<User>> result = ECommerceApp.getUserService().getAllUsers();
        
        if (result.isSuccess()) {
            usersList.addAll(result.getData());
        }
    }

    // Product Actions
    @FXML
    private void handleAddProduct() {
        showProductDialog(null);
    }

    @FXML
    private void handleEditProduct() {
        ProductDisplay selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showProductDialog(selected);
        } else {
            showAlert("No Selection", "Please select a product to edit.");
        }
    }

    @FXML
    private void handleDeleteProduct() {
        ProductDisplay selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (showConfirmation("Delete Product", "Are you sure you want to delete this product?")) {
                Result<Boolean> result = ECommerceApp.getProductService().deleteProduct(selected.getId());
                if (result.isSuccess()) {
                    showAlert("Success", "Product deleted successfully!");
                    loadProducts();
                } else {
                    showAlert("Error", result.getMessage());
                }
            }
        } else {
            showAlert("No Selection", "Please select a product to delete.");
        }
    }

    // Category Actions
    @FXML
    private void handleAddCategory() {
        showCategoryDialog(null);
    }

    @FXML
    private void handleEditCategory() {
        Categories selected = categoriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showCategoryDialog(selected);
        } else {
            showAlert("No Selection", "Please select a category to edit.");
        }
    }

    @FXML
    private void handleDeleteCategory() {
        Categories selected = categoriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (showConfirmation("Delete Category", "Are you sure you want to delete this category?")) {
                Result<Boolean> result = ECommerceApp.getCategoryService().deleteCategory(selected.getId());
                if (result.isSuccess()) {
                    showAlert("Success", "Category deleted successfully!");
                    loadCategories();
                    loadProducts(); // Refresh products as they reference categories
                } else {
                    showAlert("Error", result.getMessage());
                }
            }
        } else {
            showAlert("No Selection", "Please select a category to delete.");
        }
    }

    // User Actions
    @FXML
    private void handleAddUser() {
        showUserDialog(null);
    }

    @FXML
    private void handleRefresh() {
        loadAllData();
        showAlert("Refreshed", "All data has been refreshed successfully!");
    }

    @FXML
    private void handleLogout() {
        try {
            ECommerceApp.showLoginView();
        } catch (Exception e) {
            showAlert("Error", "Failed to logout: " + e.getMessage());
        }
    }

    private void showProductDialog(ProductDisplay product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(product == null ? "Add Product" : "Edit Product");
        dialog.setHeaderText(product == null ? "Create a new product" : "Edit product details");

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        VBox content = new VBox(10);
        TextField nameField = new TextField(product != null ? product.getName() : "");
        nameField.setPromptText("Product Name");
        TextField priceField = new TextField(product != null ? product.getPrice().toString() : "");
        priceField.setPromptText("Price");
        TextArea descField = new TextArea(product != null ? "" : "");
        descField.setPromptText("Description");
        descField.setPrefRowCount(3);
        
        ComboBox<Categories> categoryCombo = new ComboBox<>();
        categoryCombo.setItems(categoriesList);
        categoryCombo.setPromptText("Select Category");

        content.getChildren().addAll(
            new Label("Product Name:"), nameField,
            new Label("Price:"), priceField,
            new Label("Description:"), descField,
            new Label("Category:"), categoryCombo
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                Product p = new Product();
                if (product != null) p.setId(product.getId());
                p.setProductName(nameField.getText());
                p.setPrice(new BigDecimal(priceField.getText()));
                p.setDescription(descField.getText());
                p.setCategoryId(categoryCombo.getValue().getId());
                return p;
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(p -> {
            Result<Product> saveResult = product == null 
                ? ECommerceApp.getProductService().createProduct(p)
                : ECommerceApp.getProductService().updateProduct(p);
            
            if (saveResult.isSuccess()) {
                showAlert("Success", "Product saved successfully!");
                loadProducts();
            } else {
                showAlert("Error", saveResult.getMessage());
            }
        });
    }

    private void showCategoryDialog(Categories category) {
        Dialog<Categories> dialog = new Dialog<>();
        dialog.setTitle(category == null ? "Add Category" : "Edit Category");
        dialog.setHeaderText(category == null ? "Create a new category" : "Edit category details");

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        VBox content = new VBox(10);
        TextField nameField = new TextField(category != null ? category.getCategoryName() : "");
        nameField.setPromptText("Category Name");
        TextArea descField = new TextArea(category != null ? category.getDescription() : "");
        descField.setPromptText("Description");
        descField.setPrefRowCount(3);

        content.getChildren().addAll(
            new Label("Category Name:"), nameField,
            new Label("Description:"), descField
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                Categories c = new Categories();
                if (category != null) c.setId(category.getId());
                c.setCategoryName(nameField.getText());
                c.setDescription(descField.getText());
                return c;
            }
            return null;
        });

        Optional<Categories> result = dialog.showAndWait();
        result.ifPresent(c -> {
            Result<Categories> saveResult = category == null 
                ? ECommerceApp.getCategoryService().createCategory(c)
                : ECommerceApp.getCategoryService().updateCategory(c);
            
            if (saveResult.isSuccess()) {
                showAlert("Success", "Category saved successfully!");
                loadCategories();
            } else {
                showAlert("Error", saveResult.getMessage());
            }
        });
    }

    private void showUserDialog(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add User");
        dialog.setHeaderText("Create a new user");

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        VBox content = new VBox(10);
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");
        
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("CUSTOMER", "SELLER", "ADMIN");
        roleCombo.setValue("CUSTOMER");

        content.getChildren().addAll(
            new Label("First Name:"), firstNameField,
            new Label("Last Name:"), lastNameField,
            new Label("Email:"), emailField,
            new Label("Password:"), passwordField,
            new Label("Phone:"), phoneField,
            new Label("Role:"), roleCombo
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                User u = new User();
                u.setFirstname(firstNameField.getText());
                u.setLastname(lastNameField.getText());
                u.setEmail(emailField.getText());
                u.setPassword(passwordField.getText());
                u.setPhone(phoneField.getText());
                u.setUserRole(org.commerce.enums.UserRole.valueOf(roleCombo.getValue()));
                return u;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(u -> {
            Result<User> saveResult = ECommerceApp.getUserService().createUser(u);
            
            if (saveResult.isSuccess()) {
                showAlert("Success", "User created successfully!");
                loadUsers();
            } else {
                showAlert("Error", saveResult.getMessage());
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // Inner class for product display
    public static class ProductDisplay {
        private final int id;
        private final String name;
        private final BigDecimal price;
        private final int totalStock;
        private final String categoryName;

        public ProductDisplay(int id, String name, BigDecimal price, int totalStock, String categoryName) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.totalStock = totalStock;
            this.categoryName = categoryName;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public BigDecimal getPrice() { return price; }
        public String getPriceFormatted() { return "$" + price.toString(); }
        public int getTotalStock() { return totalStock; }
        public String getCategoryName() { return categoryName; }
    }
}
