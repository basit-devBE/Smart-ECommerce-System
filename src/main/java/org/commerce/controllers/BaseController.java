package org.commerce.controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Base controller providing common utility methods for all controllers.
 * Follows DRY principle by centralizing alert and confirmation dialogs.
 */
public abstract class BaseController {
    
    /**
     * Shows an information alert dialog.
     * 
     * @param title The alert title
     * @param content The alert message content
     */
    protected void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows an error alert dialog.
     * 
     * @param title The alert title
     * @param content The error message content
     */
    protected void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows a warning alert dialog.
     * 
     * @param title The alert title
     * @param content The warning message content
     */
    protected void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows a confirmation dialog.
     * 
     * @param title The confirmation title
     * @param content The confirmation message
     * @return true if user clicked OK, false otherwise
     */
    protected boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Shows a "No Selection" warning dialog.
     * 
     * @param entityName The name of the entity (e.g., "product", "category")
     * @param action The action being performed (e.g., "edit", "delete")
     */
    protected void showNoSelectionWarning(String entityName, String action) {
        showWarning("No Selection", 
                   String.format("Please select a %s to %s.", entityName, action));
    }
}
