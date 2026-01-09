package org.commerce.controllers;

import javafx.collections.FXCollections;
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
 */
public class UserController {
    
    private final TableView<User> usersTable;
    private final ObservableList<User> usersList;

    public UserController(TableView<User> usersTable) {
        this.usersTable = usersTable;
        this.usersList = FXCollections.observableArrayList();
        this.usersTable.setItems(usersList);
    }

    /**
     * Loads all users from the service and populates the table.
     */
    public void loadUsers() {
        usersList.clear();
        Result<List<User>> result = ECommerceApp.getUserService().getAllUsers();
        
        if (result.isSuccess()) {
            usersList.addAll(result.getData());
        }
    }

    /**
     * Handles add user action.
     */
    public void handleAddUser() {
        showUserDialog(null);
    }

    /**
     * Shows user add dialog.
     */
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
                DialogHelper.showAlert("Success", "User created successfully!");
                loadUsers();
            } else {
                DialogHelper.showAlert("Error", saveResult.getMessage());
            }
        });
    }
}
