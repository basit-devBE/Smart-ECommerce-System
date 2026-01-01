package org.commerce.exceptions;

/**
 * Exception thrown when a service layer operation fails.
 * This typically indicates business logic errors or validation failures.
 */
public class ServiceException extends CommerceException {
    
    public ServiceException(String message) {
        super(message);
    }
    
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
