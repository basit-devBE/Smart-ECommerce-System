package org.commerce.controllers;

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
 * Handles inventory CRUD operations and adjustments.
 * Follows Single Responsibility Principle.
 */
public class InventoryManagementController extends BaseController {
    
    private final ObservableList<InventoryDisplay> inventoryList;
    
    public InventoryManagementController(ObservableList<InventoryDisplay> inventoryList) {
        this.inventoryList = inventoryList;
    }
    
    /**
     * Loads all inventory records from the service and updates the observable list.
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
     * Handles the add inventory action.
     * 
     * @param onProductsNeedRefresh Callback to refresh products after inventory creation
     */
    public void handleAdd(Runnable onProductsNeedRefresh) {
        showInventoryDialog(onProductsNeedRefresh);
    }
    
    /**
     * Handles the adjust inventory action.
     * 
     * @param selected The selected inventory record to adjust
     * @param onProductsNeedRefresh Callback to refresh products after adjustment
     */
    public void handleAdjust(InventoryDisplay selected, Runnable onProductsNeedRefresh) {
        if (selected != null) {
            showAdjustInventoryDialog(selected, onProductsNeedRefresh);
        } else {
            showNoSelectionWarning("inventory record", "adjust");
        }
    }
    
    /**
     * Shows the inventory dialog for creating a new inventory record.
     * 
     * @param onProductsNeedRefresh Callback to refresh products after creation
     */
    private void showInventoryDialog(Runnable onProductsNeedRefresh) {
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
                try {
                    Inventory inv = new Inventory();
                    if (productCombo.getValue() != null) {
                        inv.setProductId(productCombo.getValue().getId());
                    } else {
                        showError("Validation Error", "Please select a product");
                        return null;
                    }
                    inv.setQuantity(Integer.parseInt(quantityField.getText()));
                    inv.setWarehouseLocation(warehouseField.getText());
                    return inv;
                } catch (NumberFormatException e) {
                    showError("Validation Error", "Invalid quantity. Please enter a valid number.");
                    return null;
                }
            }
            return null;
        });

        Optional<Inventory> result = dialog.showAndWait();
        result.ifPresent(inv -> {
            Result<Inventory> saveResult = ECommerceApp.getInventoryService().createInventory(inv);
            
            if (saveResult.isSuccess()) {
                showAlert("Success", "Inventory created successfully!");
                loadInventory();
                if (onProductsNeedRefresh != null) {
                    onProductsNeedRefresh.run();
                }
            } else {
                showError("Error", saveResult.getMessage());
            }
        });
    }
    
    /**
     * Shows the adjust inventory dialog.
     * 
     * @param inventory The inventory record to adjust
     * @param onProductsNeedRefresh Callback to refresh products after adjustment
     */
    private void showAdjustInventoryDialog(InventoryDisplay inventory, Runnable onProductsNeedRefresh) {
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
                    showError("Validation Error", "Invalid adjustment. Please enter a valid number.");
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
                showAlert("Success", "Inventory adjusted successfully!\nNew quantity: " + 
                         adjustResult.getData().getQuantity());
                loadInventory();
                if (onProductsNeedRefresh != null) {
                    onProductsNeedRefresh.run();
                }
            } else {
                showError("Error", adjustResult.getMessage());
            }
        });
    }
    
    /**
     * Inner class for inventory display in the table view.
     * Separates presentation concerns from domain model.
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
