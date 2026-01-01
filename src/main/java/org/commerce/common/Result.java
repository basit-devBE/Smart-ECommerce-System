package org.commerce.common;

/**
 * Generic result wrapper for service operations.
 * Represents either a success with data or a failure with error message.
 * 
 * @param <T> The type of data returned on success
 */
public class Result<T> {
    private final T data;
    private final String message;
    private final boolean success;
    
    private Result(T data, String message, boolean success) {
        this.data = data;
        this.message = message;
        this.success = success;
    }
    
    /**
     * Creates a successful result with data.
     * 
     * @param data The data to return
     * @param <T> The type of data
     * @return Successful result
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(data, null, true);
    }
    
    /**
     * Creates a successful result with data and a message.
     * 
     * @param data The data to return
     * @param message Success message
     * @param <T> The type of data
     * @return Successful result
     */
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(data, message, true);
    }
    
    /**
     * Creates a failed result with an error message.
     * 
     * @param message Error message
     * @param <T> The type of data (not used in failure)
     * @return Failed result
     */
    public static <T> Result<T> failure(String message) {
        return new Result<>(null, message, false);
    }
    
    /**
     * Checks if the operation was successful.
     * 
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Checks if the operation failed.
     * 
     * @return true if failed, false otherwise
     */
    public boolean isFailure() {
        return !success;
    }
    
    /**
     * Gets the result data.
     * Only valid if operation was successful.
     * 
     * @return The data, or null if failed
     */
    public T getData() {
        return data;
    }
    
    /**
     * Gets the result message (success or error message).
     * 
     * @return The message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Gets the data if successful, otherwise throws an exception.
     * 
     * @return The data
     * @throws IllegalStateException if the result is a failure
     */
    public T getDataOrThrow() {
        if (!success) {
            throw new IllegalStateException("Cannot get data from failed result: " + message);
        }
        return data;
    }
}
