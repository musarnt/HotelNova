package com.hotelnova.user;

import com.hotelnova.util.PasswordHasher;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for User management.
 * Handles business logic: authentication, password hashing,
 * and CRUD operations delegating persistence to UserDao.
 */
public class UserService {

    private final UserDao userDao;

    public UserService() {
        this.userDao = new UserDaoImpl();
    }

    /**
     * Authenticates a user by username and password.
     * The password is verified against the BCrypt hash stored in DB,
     * so at no point do we compare plain text passwords.
     * @param username the username entered at login
     * @param password the plain text password entered at login
     * @return the authenticated User if credentials are valid
     * @throws RuntimeException if credentials are invalid or user is inactive
     */
    public User login(String username, String password) {
        Optional<User> userOpt = userDao.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Usuario o contraseña incorrectos.");
        }

        User user = userOpt.get();

        // Check if the user account is active before allowing login
        if (!user.isActive()) {
            throw new RuntimeException("La cuenta de usuario está desactivada.");
        }

        // Verify plain password against the stored BCrypt hash
        if (!PasswordHasher.verify(password, user.getPassword())) {
            throw new RuntimeException("Usuario o contraseña incorrectos.");
        }

        return user;
    }

    /**
     * Registers a new user with a hashed password.
     * The plain text password is hashed with BCrypt BEFORE being sent to the DAO,
     * ensuring passwords are never stored in plain text.
     * @param user the user to register (with plain text password)
     */
    public void register(User user) {
        // Check if username already exists
        Optional<User> existing = userDao.findByUsername(user.getUsername());
        if (existing.isPresent()) {
            throw new RuntimeException("El nombre de usuario ya existe.");
        }

        // Hash the password before saving — NEVER store plain text
        String hashedPassword = PasswordHasher.hash(user.getPassword());
        user.setPassword(hashedPassword);

        userDao.save(user);
    }

    /**
     * Updates an existing user's information.
     * If the password has changed (not a BCrypt hash), it gets re-hashed.
     * @param user the user with updated fields
     */
    public void update(User user) {
        // If password doesn't start with $2a$ it means it's a new plain text password
        if (!user.getPassword().startsWith("$2a$")) {
            user.setPassword(PasswordHasher.hash(user.getPassword()));
        }

        userDao.update(user);
    }

    /**
     * Retrieves a user by their ID.
     * @param id the user ID
     * @return the found User
     * @throws RuntimeException if no user is found
     */
    public User findById(int id) {
        return userDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    /**
     * Lists all registered users.
     * @return list of all users
     */
    public List<User> findAll() {
        return userDao.findAll();
    }

    /**
     * Activates or deactivates a user account.
     * Deactivated users cannot log in.
     * @param id the user ID
     * @param active true to activate, false to deactivate
     */
    public void toggleActiveStatus(int id, boolean active) {
        // Verify user exists before updating
        findById(id);
        userDao.updateActiveStatus(id, active);
    }
}