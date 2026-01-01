package org.commerce.exceptions;

/**
 * Exception thrown when attempting to create an entity that already exists.
 * This is useful for unique constraint violations (e.g., duplicate email).
 */
public class DuplicateEntityException extends CommerceException {
    
    private final String entityName;
    private final String fieldName;
    private final Object fieldValue;
    
    public DuplicateEntityException(String entityName, String fieldName, Object fieldValue) {
        super(String.format("%s with %s '%s' already exists", entityName, fieldName, fieldValue));
        this.entityName = entityName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
    
    public DuplicateEntityException(String message) {
        super(message);
        this.entityName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }
    
    public String getEntityName() {
        return entityName;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public Object getFieldValue() {
        return fieldValue;
    }
}
