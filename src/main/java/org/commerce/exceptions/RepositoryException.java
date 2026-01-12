package org.commerce.exceptions;

/**
 * Exception thrown when a repository operation fails.
 * This typically wraps database-related errors (SQLException, etc.)
 */
public class RepositoryException extends CommerceException {
    
    public RepositoryException(String message) {
        super(message);
    }
    
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
