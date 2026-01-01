package org.commerce.validators;

import org.commerce.common.ValidationResult;
import org.commerce.entities.User;

/**
 * Validator for User entity.
 * Validates field-level constraints and basic business rules.
 */
public class UserValidator {
    
    /**
     * Validates a user entity.
     * 
     * @param user The user to validate
     * @return ValidationResult containing any errors found
     */
    public static ValidationResult validate(User user) {
        ValidationResult result = new ValidationResult();
        
        if (user == null) {
            result.addError("User cannot be null");
            return result;
        }
        
        // First name validation
        if (user.getFirstname() == null || user.getFirstname().trim().isEmpty()) {
            result.addError("First name is required");
        } else if (user.getFirstname().length() > 50) {
            result.addError("First name must not exceed 50 characters");
        }
        
        // Last name validation
        if (user.getLastname() == null || user.getLastname().trim().isEmpty()) {
            result.addError("Last name is required");
        } else if (user.getLastname().length() > 50) {
            result.addError("Last name must not exceed 50 characters");
        }
        
        // Email validation
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            result.addError("Email is required");
        } else if (!isValidEmail(user.getEmail())) {
            result.addError("Email format is invalid");
        }
        
        // Password validation
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            result.addError("Password is required");
        } else if (user.getPassword().length() < 6) {
            result.addError("Password must be at least 6 characters");
        }
        
        // Phone validation
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            if (user.getPhone().length() > 20) {
                result.addError("Phone number must not exceed 20 characters");
            }
        }
        
        // User role validation
        if (user.getUserRole() == null) {
            result.addError("User role is required");
        }
        
        return result;
    }
    
    /**
     * Validates email format.
     * 
     * @param email The email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Basic email regex pattern
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    /**
     * Validates a user for update operations.
     * Only validates fields that are provided (not null).
     * 
     * @param user The user to validate
     * @return ValidationResult containing any errors found
     */
    public static ValidationResult validateForUpdate(User user) {
        ValidationResult result = new ValidationResult();
        
        if (user == null) {
            result.addError("User cannot be null");
            return result;
        }
        
        if (user.getId() <= 0) {
            result.addError("Valid user ID is required for update");
        }
        
        // Only validate fields that are provided
        if (user.getFirstname() != null && !user.getFirstname().trim().isEmpty()) {
            if (user.getFirstname().length() > 50) {
                result.addError("First name must not exceed 50 characters");
            }
        }
        
        if (user.getLastname() != null && !user.getLastname().trim().isEmpty()) {
            if (user.getLastname().length() > 50) {
                result.addError("Last name must not exceed 50 characters");
            }
        }
        
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            if (!isValidEmail(user.getEmail())) {
                result.addError("Email format is invalid");
            }
        }
        
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            if (user.getPassword().length() < 6) {
                result.addError("Password must be at least 6 characters");
            }
        }
        
        return result;
    }
}
