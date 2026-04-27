package com.hotelnova.util;

import java.io.IOException;
import java.util.logging.*;

/**
 * Centralized logging utility for the entire application.
 * Writes all activity and errors to app.log file using java.util.logging.
 *
 * Usage example:
 *   AppLogger.info("Room 101 registered successfully");
 *   AppLogger.error("Failed to connect to database", exception);
 *
 * The log file is created in the project root directory.
 * Log format: [DATE] [LEVEL] [CLASS] - message
 */
public class AppLogger {

    private static final Logger logger = Logger.getLogger("HotelNova");
    private static boolean initialized = false;

    private AppLogger() {
    }

    /**
     * Initializes the logger with a file handler pointing to app.log.
     * Called automatically on first use — no manual setup needed.
     * Uses SimpleFormatter for human-readable log entries.
     */
    private static void initialize() {
        if (initialized) return;

        try {
            // Create file handler — append mode (true) so logs persist across sessions
            FileHandler fileHandler = new FileHandler("app.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Also log to console for development visibility
            logger.setUseParentHandlers(true);
            logger.setLevel(Level.ALL);

            initialized = true;
        } catch (IOException e) {
            System.err.println("Error initializing logger: " + e.getMessage());
        }
    }

    /**
     * Logs an informational message (successful operations, user actions).
     * @param message description of the event
     */
    public static void info(String message) {
        initialize();
        logger.info(message);
    }

    /**
     * Logs a warning message (non-critical issues, validation failures).
     * @param message description of the warning
     */
    public static void warn(String message) {
        initialize();
        logger.warning(message);
    }

    /**
     * Logs an error message with the exception details.
     * Captures the full stack trace in app.log for debugging.
     * @param message description of what went wrong
     * @param e the exception that was thrown
     */
    public static void error(String message, Exception e) {
        initialize();
        logger.log(Level.SEVERE, message, e);
    }

    /**
     * Logs an error message without an exception.
     * @param message description of the error
     */
    public static void error(String message) {
        initialize();
        logger.severe(message);
    }
}