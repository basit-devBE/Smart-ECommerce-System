package org.commerce.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.commerce.ECommerceApp;
import org.commerce.common.Result;
import org.commerce.daos.entities.User;
import org.commerce.enums.UserRole;

/**
 * Controller for the Registration view
 */
public class RegisterController {
    
    @FXML
    private TextField firstNameField;
    
    @FXML
    private TextField lastNameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Button registerButton;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private VBox registerBox;

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        
        // Add enter key support
        confirmPasswordField.setOnAction(event -> handleRegister());
    }

    @FXML
    private void handleRegister() {
        // Get form data
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all required fields");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters long");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Please enter a valid email address");
            return;
        }

        // Create new user with CUSTOMER role
        User newUser = new User();
        newUser.setFirstname(firstName);
        newUser.setLastname(lastName);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setPassword(password);
        newUser.setUserRole(UserRole.CUSTOMER); // Default role is CUSTOMER

        // Attempt registration
        Result<User> result = ECommerceApp.getUserService().register(newUser);

        if (result.isSuccess()) {
            User registeredUser = result.getData();
            
            // Log successful registration
            ECommerceApp.getActivityLogService().logActivity(
                registeredUser.getId(),
                registeredUser.getFirstname() + " " + registeredUser.getLastname(),
                "REGISTER",
                "USER",
                registeredUser.getId()
            );
            
            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText(null);
            alert.setContentText("Your account has been created successfully!\nYou can now sign in with your credentials.");
            alert.showAndWait();
            
            // Navigate back to login
            try {
                ECommerceApp.showLogin();
            } catch (Exception e) {
                showError("Error loading login page: " + e.getMessage());
            }
        } else {
            showError(result.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            ECommerceApp.showLogin();
        } catch (Exception e) {
            showError("Error loading login page: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        
        // Add shake animation effect
        registerBox.setTranslateX(10);
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(50), e -> registerBox.setTranslateX(-10)),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), e -> registerBox.setTranslateX(10)),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(150), e -> registerBox.setTranslateX(-10)),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(200), e -> registerBox.setTranslateX(0))
        );
        timeline.play();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}
