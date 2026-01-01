package org.commerce.services;

import java.sql.Connection;
import java.util.List;

import org.commerce.common.Result;
import org.commerce.common.ValidationResult;
import org.commerce.entities.User;
import org.commerce.exceptions.DuplicateEntityException;
import org.commerce.exceptions.EntityNotFoundException;
import org.commerce.repositories.UserRepository;
import org.commerce.repositories.interfaces.IUserRepository;
import org.commerce.validators.UserValidator;

/**
 * Service layer for User business logic.
 * Handles validation, business rules, and delegates to repository.
 */
public class UserService {
    private final Connection connection;
    private final IUserRepository userRepository;

    public UserService(Connection connection) {
        this.connection = connection;
        this.userRepository = new UserRepository();
    }

    /**
     * Creates a new user.
     * 
     * @param user The user to create
     * @return Result containing the created user or error message
     */
    public Result<User> createUser(User user) {
        // Field validation
        ValidationResult validation = UserValidator.validate(user);
        if (!validation.isValid()) {
            return Result.failure(validation.getErrorMessage());
        }
        
        // Business rule: Email must be unique
        if (userRepository.existsByEmail(user.getEmail(), connection)) {
            return Result.failure("User with email '" + user.getEmail() + "' already exists");
        }
        
        // Create user
        User created = userRepository.createUser(user, connection);
        return Result.success(created, "User created successfully");
    }

    /**
     * Deletes a user by ID.
     * 
     * @param userId The user ID
     * @return Result containing success status or error message
     */
    public Result<Boolean> deleteUser(int userId) {
        if (userId <= 0) {
            return Result.failure("Invalid user ID");
        }
        
        // Business rule: User must exist
        User userExists = userRepository.getUserById(userId, connection);
        if (userExists == null) {
            throw new EntityNotFoundException("User", userId);
        }
        
        boolean deleted = userRepository.deleteUser(userId, connection);
        return Result.success(deleted, "User deleted successfully");
    }

    /**
     * Updates an existing user.
     * 
     * @param user The user with updated information
     * @return Result containing the updated user or error message
     */
    public Result<User> updateUser(User user) {
        // Field validation
        ValidationResult validation = UserValidator.validateForUpdate(user);
        if (!validation.isValid()) {
            return Result.failure(validation.getErrorMessage());
        }

        // Business rule: User must exist
        User existingUser = userRepository.getUserById(user.getId(), connection);
        if (existingUser == null) {
            throw new EntityNotFoundException("User", user.getId());
        }

        // Merge: use new values if provided, otherwise keep existing
        if (user.getFirstname() != null && !user.getFirstname().isEmpty()) {
            existingUser.setFirstname(user.getFirstname());
        }
        if (user.getLastname() != null && !user.getLastname().isEmpty()) {
            existingUser.setLastname(user.getLastname());
        }
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            // Business rule: New email must be unique
            if (!existingUser.getEmail().equals(user.getEmail()) && 
                userRepository.existsByEmail(user.getEmail(), connection)) {
                throw new DuplicateEntityException("User", "email", user.getEmail());
            }
            existingUser.setEmail(user.getEmail());
        }
        if (user.getPassword() != null && user.getPassword().length() >= 6) {
            existingUser.setPassword(user.getPassword());
        }
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            existingUser.setPhone(user.getPhone());
        }
        if (user.getUserRole() != null) {
            existingUser.setUserRole(user.getUserRole());
        }

        User updated = userRepository.updateUser(existingUser, connection);
        return Result.success(updated, "User updated successfully");
    }

    /**
     * Retrieves a user by ID.
     * 
     * @param userId The user ID
     * @return Result containing the user or error message
     */
    public Result<User> getUserById(int userId) {
        if (userId <= 0) {
            return Result.failure("Invalid user ID");
        }

        User user = userRepository.getUserById(userId, connection);
        if (user == null) {
            throw new EntityNotFoundException("User", userId);
        }
        
        return Result.success(user);
    }

    /**
     * Retrieves all users.
     * 
     * @return Result containing list of all users
     */
    public Result<List<User>> getAllUsers() {
        List<User> users = userRepository.getAllUsers(connection);
        return Result.success(users);
    }

    /**
     * Authenticates a user with email and password.
     * 
     * @param email The user's email
     * @param password The user's password
     * @return Result containing the authenticated user or error message
     */
    public Result<User> login(String email, String password) {
        if (email == null || email.isEmpty()) {
            return Result.failure("Email is required");
        }
        
        if (password == null || password.isEmpty()) {
            return Result.failure("Password is required");
        }
        
        User user = userRepository.getUserByEmail(email, connection);
        
        if (user == null || !user.getPassword().equals(password)) {
            return Result.failure("Invalid email or password");
        }
        
        return Result.success(user, "Login successful");
    }
}
