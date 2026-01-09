package org.commerce.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.commerce.ECommerceApp;
import org.commerce.common.Result;
import org.commerce.daos.entities.Inventory;
import org.commerce.daos.entities.Product;

import java.util.List;
import java.util.Optional;

/**
 * Controller responsible for Inventory-related operations.
 * Handles inventory CRUD and adjustment operations and UI interactions.
 */
public class InventoryController {
    
    private final TableView<InventoryDisplay> inventoryTable;
    private final ObservableList<InventoryDisplay> inventoryList;
    private final Runnable onInventoryChanged;

    public InventoryController(TableView<InventoryDisplay> inventoryTable, Runnable onInventoryChanged) {
        this.inventoryTable = inventoryTable;
        this.inventoryList = FXCollections.observableArrayList();
        this.onInventoryChanged = onInventoryChanged;
        this.inventoryTable.setItems(inventoryList);
    }

    /**
     * Loads all inventory records from the service and populates the table.
     */
    public void loadInventory() {
        inventoryList.clear();
        
        Result<List<Product>> productsResult = ECommerceApp.getProductService().getAllProducts();
        if (!productsResult.isSuccess()) return;
        
        for (Product product : productsResult.getData()) {
            Result<List<Inventory>> inventoryResult = 
                ECommerceApp.getInventoryService().getInventoryByProductId(product.getId());
            
            if (inventoryResult.isSuccess()) {
                for (Inventory inv : inventoryResult.getData()) {
                    inventoryList.add(new InventoryDisplay(
                        inv.getId(),
                        inv.getProductId(),
                        product.getProductName(),
                        inv.getQuantity(),
                        inv.getWarehouseLocation()
                    ));
                }
            }
        }
    }

    /**
     * Handles add inventory action.
     */
    public void handleAddInventory() {
        showInventoryDialog();
    }

    /**
     * Handles adjust inventory action.
     */
    public void handleAdjustInventory() {
        InventoryDisplay selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showAdjustInventoryDialog(selected);
        } else {
            DialogHelper.showAlert("No Selection", "Please select an inventory record to adjust.");
        }
    }

    /**
     * Shows add inventory dialog.
     */
    private void showInventoryDialog() {
        Dialog<Inventory> dialog = new Dialog<>();
        dialog.setTitle("Add Inventory");
        dialog.setHeaderText("Create a new inventory record");

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        VBox content = new VBox(10);
        
        ComboBox<Product> productCombo = new ComboBox<>();
        Result<List<Product>> productsResult = ECommerceApp.getProductService().getAllProducts();
        if (productsResult.isSuccess()) {
            productCombo.getItems().addAll(productsResult.getData());
        }
        productCombo.setConverter(new javafx.util.StringConverter<Product>() {
            @Override
            public String toString(Product p) {
                return p != null ? p.getProductName() : "";
            }
            @Override
            public Product fromString(String string) {
                return null;
            }
        });
        productCombo.setPromptText("Select Product");
        
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        
        TextField warehouseField = new TextField();
        warehouseField.setPromptText("Warehouse Location");

        content.getChildren().addAll(
            new Label("Product:"), productCombo,
            new Label("Quantity:"), quantityField,
            new Label("Warehouse Location:"), warehouseField
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                Inventory inv = new Inventory();
                if (productCombo.getValue() != null) {
                    inv.setProductId(productCombo.getValue().getId());
                }
                try {
                    inv.setQuantity(Integer.parseInt(quantityField.getText()));
                } catch (NumberFormatException e) {
                    inv.setQuantity(0);
                }
                inv.setWarehouseLocation(warehouseField.getText());
                return inv;
            }
            return null;
        });

        Optional<Inventory> result = dialog.showAndWait();
        result.ifPresent(inv -> {
            Result<Inventory> saveResult = ECommerceApp.getInventoryService().createInventory(inv);
            
            if (saveResult.isSuccess()) {
                DialogHelper.showAlert("Success", "Inventory created successfully!");
                loadInventory();
                // Notify that inventory changed (products need to refresh stock)
                if (onInventoryChanged != null) {
                    onInventoryChanged.run();
                }
            } else {
                DialogHelper.showAlert("Error", saveResult.getMessage());
            }
        });
    }

    /**
     * Shows adjust inventory dialog.
     */
    private void showAdjustInventoryDialog(InventoryDisplay inventory) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Adjust Inventory");
        dialog.setHeaderText("Adjust inventory for: " + inventory.getProductName() + 
                            "\nCurrent Quantity: " + inventory.getQuantity());

        ButtonType saveButton = new ButtonType("Adjust", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        VBox content = new VBox(10);
        
        TextField adjustmentField = new TextField();
        adjustmentField.setPromptText("Adjustment (e.g., +10 or -5)");
        
        Label helpLabel = new Label("Enter a positive number to add, or negative to subtract");
        helpLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

        content.getChildren().addAll(
            new Label("Adjustment Amount:"), adjustmentField,
            helpLabel
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                try {
                    return Integer.parseInt(adjustmentField.getText());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Integer> result = dialog.showAndWait();
        result.ifPresent(adjustment -> {
            Result<Inventory> adjustResult = ECommerceApp.getInventoryService().adjustInventory(
                inventory.getProductId(), 
                inventory.getWarehouseLocation(), 
                adjustment
            );
            
            if (adjustResult.isSuccess()) {
                DialogHelper.showAlert("Success", "Inventory adjusted successfully!\nNew quantity: " + 
                         adjustResult.getData().getQuantity());
                loadInventory();
                // Notify that inventory changed
                if (onInventoryChanged != null) {
                    onInventoryChanged.run();
                }
            } else {
                DialogHelper.showAlert("Error", adjustResult.getMessage());
            }
        });
    }

    /**
     * Inner class for inventory display in table.
     */
    public static class InventoryDisplay {
        private final int id;
        private final int productId;
        private final String productName;
        private final int quantity;
        private final String warehouseLocation;

        public InventoryDisplay(int id, int productId, String productName, int quantity, String warehouseLocation) {
            this.id = id;
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.warehouseLocation = warehouseLocation;
        }

        public int getId() { return id; }
        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public String getWarehouseLocation() { return warehouseLocation; }
    }
}
