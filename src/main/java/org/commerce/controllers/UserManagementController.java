package org.commerce.controllers;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.commerce.ECommerceApp;
import org.commerce.common.Result;
import org.commerce.daos.entities.User;

import java.util.List;
import java.util.Optional;

/**
 * Controller responsible for User-related operations.
 * Handles user CRUD operations and UI interactions.
 * Follows Single Responsibility Principle.
 */
public class UserManagementController extends BaseController {
    
    private final ObservableList<User> usersList;
    
    public UserManagementController(ObservableList<User> usersList) {
        this.usersList = usersList;
    }
    
    /**
     * Loads all users from the service and updates the observable list.
     */
    public void loadUsers() {
        usersList.clear();
        Result<List<User>> result = ECommerceApp.getUserService().getAllUsers();
        
        if (result.isSuccess()) {
            usersList.addAll(result.getData());
        }
    }
    
    /**
     * Handles the add user action.
     */
    public void handleAdd() {
        showUserDialog(null);
    }
    
    /**
     * Handles the edit user action.
     * 
     * @param selected The selected user to edit
     */
    public void handleEdit(User selected) {
        if (selected != null) {
            showUserDialog(selected);
        } else {
            showNoSelectionWarning("user", "edit");
        }
    }
    
    /**
     * Handles the delete user action.
     * 
     * @param selected The selected user to delete
     */
    public void handleDelete(User selected) {
        if (selected != null) {
            if (showConfirmation("Delete User", "Are you sure you want to delete this user?")) {
                Result<Boolean> result = ECommerceApp.getUserService().deleteUser(selected.getId());
                if (result.isSuccess()) {
                    showAlert("Success", "User deleted successfully!");
                    loadUsers();
                } else {
                    showError("Error", result.getMessage());
                }
            }
        } else {
            showNoSelectionWarning("user", "delete");
        }
    }
    
    /**
     * Shows the user dialog for creating or editing a user.
     * 
     * @param user The user to edit, or null for new user
     */
    private void showUserDialog(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(user == null ? "Add User" : "Edit User");
        dialog.setHeaderText(user == null ? "Create a new user" : "Edit user details");

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        VBox content = new VBox(10);
        
        TextField firstNameField = new TextField(user != null ? user.getFirstname() : "");
        firstNameField.setPromptText("First Name");
        
        TextField lastNameField = new TextField(user != null ? user.getLastname() : "");
        lastNameField.setPromptText("Last Name");
        
        TextField emailField = new TextField(user != null ? user.getEmail() : "");
        emailField.setPromptText("Email");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(user == null ? "Password" : "New Password (leave empty to keep current)");
        
        TextField phoneField = new TextField(user != null ? user.getPhone() : "");
        phoneField.setPromptText("Phone");
        
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("CUSTOMER", "SELLER", "ADMIN");
        roleCombo.setValue(user != null ? user.getUserRole().toString() : "CUSTOMER");

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
                try {
                    User u = new User();
                    if (user != null) u.setId(user.getId());
                    u.setFirstname(firstNameField.getText());
                    u.setLastname(lastNameField.getText());
                    u.setEmail(emailField.getText());
                    
                    // Only set password if it's a new user or password field is not empty
                    String password = passwordField.getText();
                    if (user == null || !password.isEmpty()) {
                        u.setPassword(password);
                    }
                    
                    u.setPhone(phoneField.getText());
                    u.setUserRole(org.commerce.enums.UserRole.valueOf(roleCombo.getValue()));
                    return u;
                } catch (IllegalArgumentException e) {
                    showError("Validation Error", "Invalid role selection");
                    return null;
                }
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(u -> {
            Result<User> saveResult = user == null 
                ? ECommerceApp.getUserService().createUser(u)
                : ECommerceApp.getUserService().updateUser(u);
            
            if (saveResult.isSuccess()) {
                showAlert("Success", "User saved successfully!");
                loadUsers();
            } else {
                showError("Error", saveResult.getMessage());
            }
        });
    }
}
