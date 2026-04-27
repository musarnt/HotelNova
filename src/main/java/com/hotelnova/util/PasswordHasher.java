package com.hotelnova.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for secure password hashing using BCrypt.
 * BCrypt automatically handles salting, making each hash unique
 * even for identical passwords.
 */
public class PasswordHasher {

    // Work factor: higher = more secure but slower (10-12 is standard)
    private static final int WORK_FACTOR = 10;

    private PasswordHasher() {
    }

    /**
     * Hashes a plain text password using BCrypt.
     * @param plainPassword the raw password entered by the user
     * @return the BCrypt hashed password ready for database storage
     */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verifies a plain text password against a stored BCrypt hash.
     * Used during login to validate credentials without ever decrypting.
     * @param plainPassword the raw password entered at login
     * @param hashedPassword the stored hash from the database
     * @return true if the password matches the hash
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}