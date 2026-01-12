package org.commerce.controllers;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.commerce.ECommerceApp;
import org.commerce.common.Result;
import org.commerce.daos.entities.Categories;
import org.commerce.daos.entities.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller responsible for Product-related operations.
 * Handles product CRUD operations and UI interactions.
 * Follows Single Responsibility Principle.
 */
public class ProductManagementController extends BaseController {
    
    private final ObservableList<ProductDisplay> productsList;
    private final ObservableList<Categories> categoriesList;
    
    public ProductManagementController(ObservableList<ProductDisplay> productsList, 
                                      ObservableList<Categories> categoriesList) {
        this.productsList = productsList;
        this.categoriesList = categoriesList;
    }
    
    /**
     * Loads all products from the service and updates the observable list.
     */
    public void loadProducts() {
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
    
    /**
     * Handles the add product action.
     */
    public void handleAdd() {
        showProductDialog(null);
    }
    
    /**
     * Handles the edit product action.
     * 
     * @param selected The selected product to edit
     */
    public void handleEdit(ProductDisplay selected) {
        if (selected != null) {
            showProductDialog(selected);
        } else {
            showNoSelectionWarning("product", "edit");
        }
    }
    
    /**
     * Handles the delete product action.
     * 
     * @param selected The selected product to delete
     */
    public void handleDelete(ProductDisplay selected) {
        if (selected != null) {
            if (showConfirmation("Delete Product", "Are you sure you want to delete this product?")) {
                Result<Boolean> result = ECommerceApp.getProductService().deleteProduct(selected.getId());
                if (result.isSuccess()) {
                    showAlert("Success", "Product deleted successfully!");
                    loadProducts();
                } else {
                    showError("Error", result.getMessage());
                }
            }
        } else {
            showNoSelectionWarning("product", "delete");
        }
    }
    
    /**
     * Shows the product dialog for creating or editing a product.
     * 
     * @param product The product to edit, or null for new product
     */
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
        
        TextArea descField = new TextArea();
        descField.setPromptText("Description");
        descField.setPrefRowCount(3);
        
        ComboBox<Categories> categoryCombo = new ComboBox<>();
        categoryCombo.setItems(categoriesList);
        categoryCombo.setPromptText("Select Category");
        
        // Set converter for category combo box
        categoryCombo.setConverter(new javafx.util.StringConverter<Categories>() {
            @Override
            public String toString(Categories category) {
                return category != null ? category.getCategoryName() : "";
            }
            @Override
            public Categories fromString(String string) {
                return null;
            }
        });

        content.getChildren().addAll(
            new Label("Product Name:"), nameField,
            new Label("Price:"), priceField,
            new Label("Description:"), descField,
            new Label("Category:"), categoryCombo
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                try {
                    Product p = new Product();
                    if (product != null) p.setId(product.getId());
                    p.setProductName(nameField.getText());
                    p.setPrice(new BigDecimal(priceField.getText()));
                    p.setDescription(descField.getText());
                    
                    if (categoryCombo.getValue() != null) {
                        p.setCategoryId(categoryCombo.getValue().getId());
                    } else {
                        showError("Validation Error", "Please select a category");
                        return null;
                    }
                    return p;
                } catch (NumberFormatException e) {
                    showError("Validation Error", "Invalid price format. Please enter a valid number.");
                    return null;
                }
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
                showError("Error", saveResult.getMessage());
            }
        });
    }
    
    /**
     * Inner class for product display in the table view.
     * Separates presentation concerns from domain model.
     */
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
