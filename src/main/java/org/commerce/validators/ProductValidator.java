package org.commerce.validators;

import org.commerce.common.ValidationResult;
import org.commerce.entities.Product;

import java.math.BigDecimal;

/**
 * Validator for Product entity.
 * Validates field-level constraints and basic business rules.
 */
public class ProductValidator {
    
    /**
     * Validates a product entity.
     * 
     * @param product The product to validate
     * @return ValidationResult containing any errors found
     */
    public static ValidationResult validate(Product product) {
        ValidationResult result = new ValidationResult();
        
        if (product == null) {
            result.addError("Product cannot be null");
            return result;
        }
        
        // Product name validation
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            result.addError("Product name is required");
        } else if (product.getProductName().length() > 100) {
            result.addError("Product name must not exceed 100 characters");
        }
        
        // Price validation
        if (product.getPrice() == null) {
            result.addError("Price is required");
        } else if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            result.addError("Price must be greater than zero");
        } else if (product.getPrice().scale() > 2) {
            result.addError("Price can have at most 2 decimal places");
        }
        
        // Category ID validation
        if (product.getCategoryId() <= 0) {
            result.addError("Valid category ID is required");
        }
        
        // Description validation (optional but limited length)
        if (product.getDescription() != null && product.getDescription().length() > 1000) {
            result.addError("Description must not exceed 1000 characters");
        }
        
        return result;
    }
    
    /**
     * Validates a product for update operations.
     * Only validates fields that are provided (not null).
     * 
     * @param product The product to validate
     * @return ValidationResult containing any errors found
     */
    public static ValidationResult validateForUpdate(Product product) {
        ValidationResult result = new ValidationResult();
        
        if (product == null) {
            result.addError("Product cannot be null");
            return result;
        }
        
        if (product.getId() <= 0) {
            result.addError("Valid product ID is required for update");
        }
        
        // Only validate fields that are provided
        if (product.getProductName() != null && !product.getProductName().trim().isEmpty()) {
            if (product.getProductName().length() > 100) {
                result.addError("Product name must not exceed 100 characters");
            }
        }
        
        if (product.getPrice() != null) {
            if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                result.addError("Price must be greater than zero");
            } else if (product.getPrice().scale() > 2) {
                result.addError("Price can have at most 2 decimal places");
            }
        }
        
        if (product.getCategoryId() > 0) {
            // Valid category ID provided for update
        }
        
        if (product.getDescription() != null && product.getDescription().length() > 1000) {
            result.addError("Description must not exceed 1000 characters");
        }
        
        return result;
    }
}
