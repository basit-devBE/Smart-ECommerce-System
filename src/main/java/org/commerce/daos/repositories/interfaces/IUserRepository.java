package org.commerce.daos.repositories.interfaces;

import org.commerce.daos.entities.User;
import java.sql.Connection;
import java.util.List;

/**
 * Repository interface for User entity operations.
 */
public interface IUserRepository {
    
    /**
     * Creates a new user in the database.
     * 
     * @param user The user to create
     * @param connection The database connection
     * @return The created user with generated ID and timestamps
     */
    User createUser(User user, Connection connection);
    
    /**
     * Retrieves a user by their ID.
     * 
     * @param userId The user ID
     * @param connection The database connection
     * @return The user if found, null otherwise
     */
    User getUserById(int userId, Connection connection);
    
    /**
     * Retrieves a user by their email address.
     * 
     * @param email The user's email
     * @param connection The database connection
     * @return The user if found, null otherwise
     */
    User getUserByEmail(String email, Connection connection);
    
    /**
     * Retrieves all users from the database.
     * 
     * @param connection The database connection
     * @return List of all users
     */
    List<User> getAllUsers(Connection connection);
    
    /**
     * Updates an existing user.
     * 
     * @param user The user with updated information
     * @param connection The database connection
     * @return The updated user
     */
    User updateUser(User user, Connection connection);
    
    /**
     * Deletes a user by their ID.
     * 
     * @param userId The user ID
     * @param connection The database connection
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteUser(int userId, Connection connection);
    
    /**
     * Checks if a user with the given email exists.
     * 
     * @param email The email to check
     * @param connection The database connection
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email, Connection connection);
}
