package com.hotelnova.util;

/**
 * Simulates HTTP request traces for CRUD operations.
 * Logs each operation as if it were an HTTP call, following REST conventions.
 *
 * This is required by the assignment to simulate "llamadas HTTP" in console/logs.
 *
 * Output format example:
 *   [POST]   /api/rooms         → 201 CREATED    | Room 101 registered
 *   [GET]    /api/rooms         → 200 OK          | Listed 15 rooms
 *   [PATCH]  /api/rooms/5       → 200 OK          | Room 105 updated
 *   [DELETE] /api/guests/3      → 200 OK          | Guest deactivated
 */
public class HttpLogger {

    private HttpLogger() {
    }

    /**
     * Logs a simulated POST request (CREATE operations).
     * @param endpoint the REST endpoint (e.g., "/api/rooms")
     * @param detail description of what was created
     */
    public static void post(String endpoint, String detail) {
        log("POST", endpoint, "201 CREATED", detail);
    }

    /**
     * Logs a simulated GET request (READ operations).
     * @param endpoint the REST endpoint
     * @param detail description of what was retrieved
     */
    public static void get(String endpoint, String detail) {
        log("GET", endpoint, "200 OK", detail);
    }

    /**
     * Logs a simulated PATCH request (UPDATE operations).
     * @param endpoint the REST endpoint with ID (e.g., "/api/rooms/5")
     * @param detail description of what was updated
     */
    public static void patch(String endpoint, String detail) {
        log("PATCH", endpoint, "200 OK", detail);
    }

    /**
     * Logs a simulated DELETE request (DELETE/deactivate operations).
     * @param endpoint the REST endpoint with ID
     * @param detail description of what was deleted/deactivated
     */
    public static void delete(String endpoint, String detail) {
        log("DELETE", endpoint, "200 OK", detail);
    }

    /**
     * Logs a simulated failed request.
     * @param method the HTTP method that failed
     * @param endpoint the REST endpoint
     * @param detail error description
     */
    public static void error(String method, String endpoint, String detail) {
        log(method, endpoint, "400 BAD REQUEST", detail);
    }

    /**
     * Formats and prints the simulated HTTP log entry.
     * Also writes to app.log via AppLogger for persistent record.
     */
    private static void log(String method, String endpoint, String statusCode, String detail) {
        String entry = String.format("[%-6s] %-25s → %-15s | %s", method, endpoint, statusCode, detail);
        System.out.println(entry);
        AppLogger.info(entry);
    }
}