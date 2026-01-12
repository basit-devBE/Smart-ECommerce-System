package org.commerce.validators;

import org.commerce.common.ValidationResult;
import org.commerce.daos.entities.Inventory;

/**
 * Validator for Inventory entity.
 * Validates field-level constraints and basic business rules.
 */
public class InventoryValidator {
    
    /**
     * Validates an inventory entity.
     * 
     * @param inventory The inventory to validate
     * @return ValidationResult containing any errors found
     */
    public static ValidationResult validate(Inventory inventory) {
        ValidationResult result = new ValidationResult();
        
        if (inventory == null) {
            result.addError("Inventory cannot be null");
            return result;
        }
        
        // Product ID validation
        if (inventory.getProductId() <= 0) {
            result.addError("Valid product ID is required");
        }
        
        // Quantity validation
        if (inventory.getQuantity() < 0) {
            result.addError("Quantity cannot be negative");
        }
        
        // Warehouse location validation
        if (inventory.getWarehouseLocation() == null || inventory.getWarehouseLocation().trim().isEmpty()) {
            result.addError("Warehouse location is required");
        } else if (inventory.getWarehouseLocation().length() > 100) {
            result.addError("Warehouse location must not exceed 100 characters");
        }
        
        return result;
    }
    
    /**
     * Validates an inventory for update operations.
     * Only validates fields that are provided.
     * 
     * @param inventory The inventory to validate
     * @return ValidationResult containing any errors found
     */
    public static ValidationResult validateForUpdate(Inventory inventory) {
        ValidationResult result = new ValidationResult();
        
        if (inventory == null) {
            result.addError("Inventory cannot be null");
            return result;
        }
        
        if (inventory.getId() <= 0) {
            result.addError("Valid inventory ID is required for update");
        }
        
        // Quantity validation
        if (inventory.getQuantity() < 0) {
            result.addError("Quantity cannot be negative");
        }
        
        // Warehouse location validation (if provided)
        if (inventory.getWarehouseLocation() != null && !inventory.getWarehouseLocation().trim().isEmpty()) {
            if (inventory.getWarehouseLocation().length() > 100) {
                result.addError("Warehouse location must not exceed 100 characters");
            }
        }
        
        return result;
    }
}
