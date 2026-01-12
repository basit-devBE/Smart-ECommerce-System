package org.commerce.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.commerce.ECommerceApp;
import org.commerce.common.Result;
import org.commerce.daos.entities.User;
import org.commerce.enums.UserRole;

/**
 * Controller for the Login view
 */
public class LoginController {
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private VBox loginBox;
    
    private static User currentUser;

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        
        // Add enter key support
        passwordField.setOnAction(event -> handleLogin());
        emailField.setOnAction(event -> passwordField.requestFocus());
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }

        // Attempt login
        Result<User> result = ECommerceApp.getUserService().login(email, password);

        if (result.isSuccess()) {
            currentUser = result.getData();
            
            // Log successful login
            ECommerceApp.getActivityLogService().logActivity(
                currentUser.getId(),
                currentUser.getFirstname() + " " + currentUser.getLastname(),
                "LOGIN",
                "USER",
                currentUser.getId()
            );
            
            // Route based on user role
            try {
                if (currentUser.getUserRole() == UserRole.CUSTOMER) {
                    // Customers see product listing page
                    ECommerceApp.showProductListing();
                } else {
                    // Admin and Seller see dashboard
                    ECommerceApp.showDashboard();
                }
            } catch (Exception e) {
                showError("Error loading application: " + e.getMessage());
            }
        } else {
            // Log failed login attempt
            ECommerceApp.getActivityLogService().logActivity(
                0,
                email,
                "LOGIN_FAILED"
            );
            
            showError(result.getMessage());
            passwordField.clear();
        }
    }

    @FXML
    private void handleRegister() {
        try {
            ECommerceApp.showRegister();
        } catch (Exception e) {
            showError("Error loading registration page: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        
        // Add shake animation effect
        loginBox.setTranslateX(10);
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(50), e -> loginBox.setTranslateX(-10)),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), e -> loginBox.setTranslateX(10)),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(150), e -> loginBox.setTranslateX(-10)),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(200), e -> loginBox.setTranslateX(0))
        );
        timeline.play();
    }

    public static User getCurrentUser() {
        return currentUser;
    }
}
