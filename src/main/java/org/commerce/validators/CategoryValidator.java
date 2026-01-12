package org.commerce.validators;

import org.commerce.common.ValidationResult;
import org.commerce.daos.entities.Categories;

/**
 * Validator for Categories entity.
 * Validates field-level constraints and basic business rules.
 */
public class CategoryValidator {
    
    /**
     * Validates a category entity.
     * 
     * @param category The category to validate
     * @return ValidationResult containing any errors found
     */
    public static ValidationResult validate(Categories category) {
        ValidationResult result = new ValidationResult();
        
        if (category == null) {
            result.addError("Category cannot be null");
            return result;
        }
        
        // Category name validation
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            result.addError("Category name is required");
        } else if (category.getCategoryName().length() > 100) {
            result.addError("Category name must not exceed 100 characters");
        }
        
        // Description validation (optional but limited length)
        if (category.getDescription() != null && category.getDescription().length() > 500) {
            result.addError("Description must not exceed 500 characters");
        }
        
        return result;
    }
    
    /**
     * Validates a category for update operations.
     * Only validates fields that are provided (not null).
     * 
     * @param category The category to validate
     * @return ValidationResult containing any errors found
     */
    public static ValidationResult validateForUpdate(Categories category) {
        ValidationResult result = new ValidationResult();
        
        if (category == null) {
            result.addError("Category cannot be null");
            return result;
        }
        
        if (category.getId() <= 0) {
            result.addError("Valid category ID is required for update");
        }
        
        // Only validate fields that are provided
        if (category.getCategoryName() != null && !category.getCategoryName().trim().isEmpty()) {
            if (category.getCategoryName().length() > 100) {
                result.addError("Category name must not exceed 100 characters");
            }
        }
        
        if (category.getDescription() != null && category.getDescription().length() > 500) {
            result.addError("Description must not exceed 500 characters");
        }
        
        return result;
    }
}
