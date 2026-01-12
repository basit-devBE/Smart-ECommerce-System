package org.commerce.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for validation results.
 * Collects multiple validation error messages.
 */
public class ValidationResult {
    private final List<String> errors;
    
    public ValidationResult() {
        this.errors = new ArrayList<>();
    }
    
    /**
     * Adds a validation error message.
     * 
     * @param error The error message to add
     */
    public void addError(String error) {
        if (error != null && !error.trim().isEmpty()) {
            errors.add(error);
        }
    }
    
    /**
     * Checks if validation passed (no errors).
     * 
     * @return true if no errors, false otherwise
     */
    public boolean isValid() {
        return errors.isEmpty();
    }
    
    /**
     * Gets all validation errors.
     * 
     * @return List of error messages
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    /**
     * Gets all errors as a single string, separated by commas.
     * 
     * @return Combined error message
     */
    public String getErrorMessage() {
        return String.join(", ", errors);
    }
    
    /**
     * Checks if there are multiple errors.
     * 
     * @return true if more than one error exists
     */
    public boolean hasMultipleErrors() {
        return errors.size() > 1;
    }
    
    /**
     * Gets the number of errors.
     * 
     * @return Error count
     */
    public int getErrorCount() {
        return errors.size();
    }
}
