package com.hotelnova.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Centralized configuration provider for the application.
 * Reads all settings from config.properties at first access and caches them
 * for the lifetime of the application. Using a static utility class (no instances)
 * keeps config access simple and consistent across all layers without dependency injection.
 */
public class AppConfig {

    private static final Properties properties = new Properties();

    // Lazy-loaded flag to avoid reading the file on class load —
    // only pays the I/O cost if config is actually needed at runtime.
    private static boolean loaded = false;

    // Utility class — no instances should ever be created.
    private AppConfig() {
    }

    /**
     * Loads config.properties from the classpath on the first call.
     * Subsequent calls are no-ops thanks to the loaded guard.
     * Fails fast with a RuntimeException so misconfigured environments
     * are caught at startup rather than silently producing wrong behavior later.
     */
    private static void loadProperties() {
        if (loaded) return;
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in resources");
            }
            properties.load(input);
            loaded = true;
        } catch (IOException e) {
            throw new RuntimeException("Error loading config.properties: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the raw string value for the given key.
     * Prefer the typed getters below over calling this directly —
     * they handle parsing and make intent clearer at the call site.
     */
    public static String get(String key) {
        loadProperties();
        return properties.getProperty(key);
    }

    // --- Database ---

    public static String getDbUrl() {
        return get("db.url");
    }

    public static String getDbUser() {
        return get("db.user");
    }

    public static String getDbPassword() {
        return get("db.password");
    }

    // Business rules

    // Check-in/out hours are stored as plain integers (e.g. 15 for 3 PM)
    // to simplify time comparisons without needing a full LocalTime object.
    public static int getCheckInHour() {
        return Integer.parseInt(get("horaCheckIn"));
    }

    public static int getCheckOutHour() {
        return Integer.parseInt(get("horaCheckOut"));
    }

    // IVA is kept in config (not hardcoded) so it can be updated
    // without recompiling when tax rates change.
    public static double getIva() {
        return Double.parseDouble(get("iva"));
    }
}