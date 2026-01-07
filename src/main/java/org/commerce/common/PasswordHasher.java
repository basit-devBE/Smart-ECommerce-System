package org.commerce.common;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for hashing and verifying passwords using BCrypt.
 * BCrypt automatically handles salt generation and is designed to be slow
 * to resist brute-force attacks.
 */
public class PasswordHasher {
    
    // BCrypt work factor (log2 rounds). 12 means 2^12 = 4096 rounds
    // Higher values = more secure but slower. 12 is a good balance.
    private static final int BCRYPT_WORK_FACTOR = 12;
    
    /**
     * Hashes a plain text password using BCrypt.
     * 
     * @param plainPassword The plain text password to hash
     * @return The hashed password
     * @throws IllegalArgumentException if password is null or empty
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        // Generate salt and hash password in one step
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_WORK_FACTOR));
    }
    
    /**
     * Verifies a plain text password against a hashed password.
     * 
     * @param plainPassword The plain text password to verify
     * @param hashedPassword The hashed password to check against
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return false;
        }
        
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Invalid hash format
            return false;
        }
    }
    
    /**
     * Checks if a password needs to be rehashed (e.g., work factor changed).
     * 
     * @param hashedPassword The hashed password to check
     * @return true if the password should be rehashed
     */
    public static boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            return true;
        }
        
        try {
            // Extract work factor from hash
            String[] parts = hashedPassword.split("\\$");
            if (parts.length < 4) {
                return true;
            }
            
            int workFactor = Integer.parseInt(parts[2]);
            return workFactor != BCRYPT_WORK_FACTOR;
        } catch (Exception e) {
            return true;
        }
    }
}
