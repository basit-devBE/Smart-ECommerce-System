package org.commerce.exceptions;

/**
 * Exception thrown when a requested entity is not found in the database.
 * This is useful for RESTful operations where 404 responses are needed.
 */
public class EntityNotFoundException extends CommerceException {
    
    private final String entityName;
    private final Object entityId;
    
    public EntityNotFoundException(String entityName, Object entityId) {
        super(String.format("%s with ID %s not found", entityName, entityId));
        this.entityName = entityName;
        this.entityId = entityId;
    }
    
    public EntityNotFoundException(String message) {
        super(message);
        this.entityName = null;
        this.entityId = null;
    }
    
    public String getEntityName() {
        return entityName;
    }
    
    public Object getEntityId() {
        return entityId;
    }
}
