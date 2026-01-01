package org.commerce.exceptions;

import java.util.List;

/**
 * Exception thrown when validation fails.
 * Can contain multiple validation error messages.
 */
public class ValidationException extends CommerceException {
    
    private final List<String> errors;
    
    public ValidationException(String message) {
        super(message);
        this.errors = List.of(message);
    }
    
    public ValidationException(List<String> errors) {
        super(String.join(", ", errors));
        this.errors = errors;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public boolean hasMultipleErrors() {
        return errors.size() > 1;
    }
}
