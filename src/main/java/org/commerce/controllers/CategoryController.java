package org.commerce.controllers;

import javafx.collections.FXCollections;
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
 */
public class CategoryController {
    
    private final TableView<Categories> categoriesTable;
    private final ObservableList<Categories> categoriesList;
    private final Runnable onCategoryChanged;

    public CategoryController(TableView<Categories> categoriesTable, Runnable onCategoryChanged) {
        this.categoriesTable = categoriesTable;
        this.categoriesList = FXCollections.observableArrayList();
        this.onCategoryChanged = onCategoryChanged;
        this.categoriesTable.setItems(categoriesList);
    }

    /**
     * Gets the observable list of categories for use by other controllers.
     */
    public ObservableList<Categories> getCategoriesList() {
        return categoriesList;
    }

    /**
     * Loads all categories from the service and populates the table.
     */
    public void loadCategories() {
        categoriesList.clear();
        Result<List<Categories>> result = ECommerceApp.getCategoryService().getAllCategories();
        
        if (result.isSuccess()) {
            categoriesList.addAll(result.getData());
        }
    }

    /**
     * Handles add category action.
     */
    public void handleAddCategory() {
        showCategoryDialog(null);
    }

    /**
     * Handles edit category action.
     */
    public void handleEditCategory() {
        Categories selected = categoriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showCategoryDialog(selected);
        } else {
            DialogHelper.showAlert("No Selection", "Please select a category to edit.");
        }
    }

    /**
     * Handles delete category action.
     */
    public void handleDeleteCategory() {
        Categories selected = categoriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (DialogHelper.showConfirmation("Delete Category", "Are you sure you want to delete this category?")) {
                Result<Boolean> result = ECommerceApp.getCategoryService().deleteCategory(selected.getId());
                if (result.isSuccess()) {
                    DialogHelper.showAlert("Success", "Category deleted successfully!");
                    loadCategories();
                    // Notify that categories changed (products need to refresh)
                    if (onCategoryChanged != null) {
                        onCategoryChanged.run();
                    }
                } else {
                    DialogHelper.showAlert("Error", result.getMessage());
                }
            }
        } else {
            DialogHelper.showAlert("No Selection", "Please select a category to delete.");
        }
    }

    /**
     * Shows category add/edit dialog.
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
                DialogHelper.showAlert("Success", "Category saved successfully!");
                loadCategories();
            } else {
                DialogHelper.showAlert("Error", saveResult.getMessage());
            }
        });
    }
}
