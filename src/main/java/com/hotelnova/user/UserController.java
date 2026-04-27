package com.hotelnova.user;

import java.util.List;

/**
 * Controller layer for User operations.
 * Acts as a bridge between the UI (JavaFX views) and the business logic (UserService).
 * Each method handles a specific user action, catches exceptions from the service layer,
 * and returns results or error messages to the UI.
 */
public class UserController {

    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    /**
     * Handles user login attempt.
     * Delegates authentication to UserService which validates credentials with BCrypt.
     * @param username the entered username
     * @param password the entered plain text password
     * @return the authenticated User object (contains role for menu access control)
     * @throws RuntimeException propagated from service if credentials are invalid
     */
    public User login(String username, String password) {
        return userService.login(username, password);
    }

    /**
     * Handles new user registration.
     * The service layer will hash the password before saving.
     * @param username desired username
     * @param password plain text password (will be hashed by service)
     * @param role the role to assign (ADMIN or RECEPTIONIST)
     */
    public void register(String username, String password, UserRole role) {
        User user = new User(username, password, role);
        userService.register(user);
    }

    /**
     * Handles user update.
     * @param user the user with modified fields
     */
    public void update(User user) {
        userService.update(user);
    }

    /**
     * Retrieves a user by ID.
     * @param id the user ID
     * @return the found User
     */
    public User findById(int id) {
        return userService.findById(id);
    }

    /**
     * Lists all users in the system.
     * @return list of all users
     */
    public List<User> findAll() {
        return userService.findAll();
    }

    /**
     * Toggles a user's active status (activate/deactivate).
     * @param id the user ID
     * @param active true to activate, false to deactivate
     */
    public void toggleActiveStatus(int id, boolean active) {
        userService.toggleActiveStatus(id, active);
    }
}