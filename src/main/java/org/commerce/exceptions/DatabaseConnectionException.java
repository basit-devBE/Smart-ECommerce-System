package org.commerce.exceptions;

/**
 * Exception thrown when database connection fails.
 * This typically indicates configuration issues or database unavailability.
 */
public class DatabaseConnectionException extends CommerceException {
    
    public DatabaseConnectionException(String message) {
        super(message);
    }
    
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
