package com.ncslab.util;

/**
 * Thread-local storage for user context in multi-user WebSocket environment.
 *
 * <p>Each WebSocket request runs in its own thread, allowing safe per-request user tracking.
 * This utility provides a clean way to propagate user identity through the call stack
 * without modifying method signatures.
 *
 * <h3>Usage Pattern:</h3>
 * <pre>{@code
 * try {
 *     UserContext.setUserId("123");
 *     // ... perform operations that need user context ...
 * } finally {
 *     UserContext.clear(); // Always clean up to prevent memory leaks
 * }
 * }</pre>
 *
 * <h3>Thread Safety:</h3>
 * <ul>
 *   <li>Each thread maintains its own user context</li>
 *   <li>No synchronization required</li>
 *   <li>Safe for concurrent WebSocket sessions</li>
 * </ul>
 *
 * <h3>Memory Management:</h3>
 * <p>Always call {@link #clear()} in a finally block to prevent ThreadLocal memory leaks,
 * especially in server environments with thread pools.
 *
 * @author NCSLabLink Development Team
 * @version 2025.1
 * @since 2025-01-03
 */
public class UserContext {

    /**
     * ThreadLocal storage for current user ID.
     * Each thread maintains its own independent user context.
     */
    private static final ThreadLocal<String> currentUserId = new ThreadLocal<>();

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private UserContext() {
        throw new UnsupportedOperationException("UserContext is a utility class and cannot be instantiated");
    }

    /**
     * Set the user ID for the current thread context.
     *
     * @param userId The user ID to set (can be null to clear)
     * @throws IllegalArgumentException if userId is empty string
     */
    public static void setUserId(String userId) {
        if (userId != null && userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty string");
        }
        currentUserId.set(userId);
    }

    /**
     * Set the user ID from an Integer value.
     * Convenience method for converting Integer user IDs to String.
     *
     * @param userId The user ID as Integer (null safe)
     */
    public static void setUserId(Integer userId) {
        if (userId != null) {
            setUserId(String.valueOf(userId));
        } else {
            currentUserId.set(null);
        }
    }

    /**
     * Get the user ID for the current thread context.
     *
     * @return The user ID for current thread, or null if not set
     */
    public static String getUserId() {
        return currentUserId.get();
    }

    /**
     * Get the user ID as an Integer.
     * Convenience method for converting String user IDs to Integer.
     *
     * @return The user ID as Integer, or null if not set or not parseable
     */
    public static Integer getUserIdAsInteger() {
        String userId = currentUserId.get();
        if (userId == null) {
            return null;
        }
        try {
            return Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Clear the user context for the current thread.
     *
     * <p><strong>IMPORTANT:</strong> Always call this method in a finally block
     * to prevent ThreadLocal memory leaks. Thread pools reuse threads, so
     * failure to clear can cause context bleeding between requests.
     *
     * <p>Example:
     * <pre>{@code
     * try {
     *     UserContext.setUserId("123");
     *     // ... do work ...
     * } finally {
     *     UserContext.clear(); // Critical cleanup
     * }
     * }</pre>
     */
    public static void clear() {
        currentUserId.remove();
    }

    /**
     * Check if a user context is currently set.
     *
     * @return true if user ID is set, false otherwise
     */
    public static boolean isSet() {
        return currentUserId.get() != null;
    }

    /**
     * Get the user ID with a fallback value if not set.
     *
     * @param defaultUserId The default user ID to return if not set
     * @return The current user ID, or defaultUserId if not set
     */
    public static String getUserIdOrDefault(String defaultUserId) {
        String userId = currentUserId.get();
        return userId != null ? userId : defaultUserId;
    }

    /**
     * Get debugging information about current thread context.
     * Useful for troubleshooting user context issues.
     *
     * @return Debug string with thread and user context info
     */
    public static String getDebugInfo() {
        return String.format("Thread[%s] UserId[%s]",
            Thread.currentThread().getName(),
            currentUserId.get() != null ? currentUserId.get() : "NOT_SET");
    }
}
