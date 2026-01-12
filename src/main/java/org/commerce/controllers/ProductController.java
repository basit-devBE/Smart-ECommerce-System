package org.commerce.controllers;

import javafx.collections.FXCollections;
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
 */
public class ProductController {
    
    private final TableView<ProductDisplay> productsTable;
    private final ObservableList<ProductDisplay> productsList;
    private final ObservableList<Categories> categoriesList;

    public ProductController(TableView<ProductDisplay> productsTable, 
                            ObservableList<Categories> categoriesList) {
        this.productsTable = productsTable;
        this.productsList = FXCollections.observableArrayList();
        this.categoriesList = categoriesList;
        this.productsTable.setItems(productsList);
    }

    /**
     * Loads all products from the service and populates the table.
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
     * Handles add product action.
     */
    public void handleAddProduct() {
        showProductDialog(null);
    }

    /**
     * Handles edit product action.
     */
    public void handleEditProduct() {
        ProductDisplay selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showProductDialog(selected);
        } else {
            DialogHelper.showAlert("No Selection", "Please select a product to edit.");
        }
    }

    /**
     * Handles delete product action.
     */
    public void handleDeleteProduct() {
        ProductDisplay selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (DialogHelper.showConfirmation("Delete Product", "Are you sure you want to delete this product?")) {
                Result<Boolean> result = ECommerceApp.getProductService().deleteProduct(selected.getId());
                if (result.isSuccess()) {
                    DialogHelper.showAlert("Success", "Product deleted successfully!");
                    loadProducts();
                } else {
                    DialogHelper.showAlert("Error", result.getMessage());
                }
            }
        } else {
            DialogHelper.showAlert("No Selection", "Please select a product to delete.");
        }
    }

    /**
     * Shows product add/edit dialog.
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
                DialogHelper.showAlert("Success", "Product saved successfully!");
                loadProducts();
            } else {
                DialogHelper.showAlert("Error", saveResult.getMessage());
            }
        });
    }

    /**
     * Inner class for product display in table.
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
