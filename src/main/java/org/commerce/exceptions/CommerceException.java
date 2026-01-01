package org.commerce.exceptions;

/**
 * Base unchecked exception for the Commerce application.
 * All custom exceptions in the application should extend this class.
 */
public class CommerceException extends RuntimeException {
    
    public CommerceException(String message) {
        super(message);
    }
    
    public CommerceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CommerceException(Throwable cause) {
        super(cause);
    }
}
