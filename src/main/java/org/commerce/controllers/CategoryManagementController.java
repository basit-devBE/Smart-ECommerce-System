package org.commerce.controllers;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.commerce.ECommerceApp;
import org.commerce.common.Result;
import org.commerce.daos.entities.Categories;

import java.util.List;
import java.util.Optional;

/**
 * Controller responsible for Category-related operations.
 * Handles category CRUD operations and UI interactions.
 * Follows Single Responsibility Principle.
 */
public class CategoryManagementController extends BaseController {
    
    private final ObservableList<Categories> categoriesList;
    
    public CategoryManagementController(ObservableList<Categories> categoriesList) {
        this.categoriesList = categoriesList;
    }
    
    /**
     * Loads all categories from the service and updates the observable list.
     */
    public void loadCategories() {
        categoriesList.clear();
        Result<List<Categories>> result = ECommerceApp.getCategoryService().getAllCategories();
        
        if (result.isSuccess()) {
            categoriesList.addAll(result.getData());
        }
    }
    
    /**
     * Handles the add category action.
     */
    public void handleAdd() {
        showCategoryDialog(null);
    }
    
    /**
     * Handles the edit category action.
     * 
     * @param selected The selected category to edit
     */
    public void handleEdit(Categories selected) {
        if (selected != null) {
            showCategoryDialog(selected);
        } else {
            showNoSelectionWarning("category", "edit");
        }
    }
    
    /**
     * Handles the delete category action.
     * 
     * @param selected The selected category to delete
     * @param onProductsNeedRefresh Callback to refresh products after category deletion
     */
    public void handleDelete(Categories selected, Runnable onProductsNeedRefresh) {
        if (selected != null) {
            if (showConfirmation("Delete Category", "Are you sure you want to delete this category?")) {
                Result<Boolean> result = ECommerceApp.getCategoryService().deleteCategory(selected.getId());
                if (result.isSuccess()) {
                    showAlert("Success", "Category deleted successfully!");
                    loadCategories();
                    if (onProductsNeedRefresh != null) {
                        onProductsNeedRefresh.run(); // Refresh products as they reference categories
                    }
                } else {
                    showError("Error", result.getMessage());
                }
            }
        } else {
            showNoSelectionWarning("category", "delete");
        }
    }
    
    /**
     * Shows the category dialog for creating or editing a category.
     * 
     * @param category The category to edit, or null for new category
     */
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
                showError("Error", saveResult.getMessage());
            }
        });
    }
}
