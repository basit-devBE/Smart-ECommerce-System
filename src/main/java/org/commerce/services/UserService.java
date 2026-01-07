package org.commerce.services;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.commerce.common.CacheManager;
import org.commerce.common.PasswordHasher;
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
 * Implements session-based caching for logged-in users.
 */
public class UserService {
    private final Connection connection;
    private final IUserRepository userRepository;
    
    // Cache for individual users (by ID) - 10 minute TTL, max 100 entries
    private final CacheManager<Integer, User> userCache;
    
    // Cache for user lookups by email - 5 minute TTL, max 100 entries
    private final CacheManager<String, User> emailCache;
    
    // In-memory Map for active user sessions
    private final Map<Integer, User> activeSessionsCache;

    public UserService(Connection connection) {
        this.connection = connection;
        this.userRepository = new UserRepository();
        this.userCache = new CacheManager<>(600000, 100); // 10 min, 100 entries
        this.emailCache = new CacheManager<>(300000, 100); // 5 min, 100 entries
        this.activeSessionsCache = new HashMap<>();
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
        
        // Hash password before storing
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(PasswordHasher.hashPassword(user.getPassword()));
        }
        
        // Create user
        User created = userRepository.createUser(user, connection);
        
        // Invalidate caches after creation
        invalidateAllCaches();
        
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
        
        // Invalidate caches and remove from active sessions
        invalidateAllCaches();
        activeSessionsCache.remove(userId);
        
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
            // Hash the new password before storing
            existingUser.setPassword(PasswordHasher.hashPassword(user.getPassword()));
        }
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            existingUser.setPhone(user.getPhone());
        }
        if (user.getUserRole() != null) {
            existingUser.setUserRole(user.getUserRole());
        }

        User updated = userRepository.updateUser(existingUser, connection);
        
        // Invalidate caches and update active session
        invalidateAllCaches();
        if (activeSessionsCache.containsKey(updated.getId())) {
            activeSessionsCache.put(updated.getId(), updated);
        }
        
        return Result.success(updated, "User updated successfully");
    }

    /**
     * Retrieves a user by ID (with caching).
     * 
     * @param userId The user ID
     * @return Result containing the user or error message
     */
    public Result<User> getUserById(int userId) {
        if (userId <= 0) {
            return Result.failure("Invalid user ID");
        }

        // Check active sessions first (fastest)
        if (activeSessionsCache.containsKey(userId)) {
            return Result.success(activeSessionsCache.get(userId));
        }

        // Then check cache
        User user = userCache.get(userId, () -> 
            userRepository.getUserById(userId, connection)
        );
        
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
     * Stores user in active session cache upon successful login.
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
        
        // Try email cache first
        User user = emailCache.get(email, () -> 
            userRepository.getUserByEmail(email, connection)
        );
        
        if (user == null) {
            return Result.failure("Invalid email or password");
        }
        
        // Verify password using BCrypt
        if (!PasswordHasher.verifyPassword(password, user.getPassword())) {
            return Result.failure("Invalid email or password");
        }
        
        // Store in active sessions for fast access
        activeSessionsCache.put(user.getId(), user);
        
        return Result.success(user, "Login successful");
    }
    
    /**
     * Logs out a user by removing from active session cache.
     * 
     * @param userId The user ID
     */
    public void logout(int userId) {
        activeSessionsCache.remove(userId);
    }
    
    /**
     * Gets the currently active user from session cache.
     * 
     * @param userId The user ID
     * @return User if in active session, null otherwise
     */
    public User getActiveUser(int userId) {
        return activeSessionsCache.get(userId);
    }
    
    /**
     * Gets count of active user sessions.
     */
    public int getActiveSessionCount() {
        return activeSessionsCache.size();
    }
    
    /**
     * Invalidates all user caches.
     * Should be called after any create, update, or delete operation.
     */
    public void invalidateAllCaches() {
        userCache.invalidateAll();
        emailCache.invalidateAll();
    }
    
    /**
     * Gets cache statistics.
     */
    public String getCacheStats() {
        return String.format(
            "User Cache: %d entries, Email Cache: %d entries, Active Sessions: %d users",
            userCache.size(), emailCache.size(), activeSessionsCache.size()
        );
    }
}
