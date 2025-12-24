package com.ncslab.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for UserContext ThreadLocal management.
 * Tests thread isolation, memory leak prevention, and multi-user scenarios.
 */
public class UserContextTest {

    @Before
    public void setUp() {
        // Ensure clean state before each test
        UserContext.clear();
    }

    @After
    public void tearDown() {
        // Clean up after each test to prevent pollution
        UserContext.clear();
    }

    @Test
    public void testSetAndGetUserId() {
        // Test basic set and get
        assertNull("Initial user ID should be null", UserContext.getUserId());

        UserContext.setUserId("123");
        assertEquals("User ID should be set", "123", UserContext.getUserId());

        // Test overwriting
        UserContext.setUserId("456");
        assertEquals("User ID should be updated", "456", UserContext.getUserId());
    }

    @Test
    public void testSetUserIdFromInteger() {
        // Test Integer to String conversion
        UserContext.setUserId(789);
        assertEquals("User ID should be converted from Integer", "789", UserContext.getUserId());

        // Test null Integer
        UserContext.setUserId((Integer) null);
        assertNull("User ID should be null after setting null Integer", UserContext.getUserId());
    }

    @Test
    public void testGetUserIdAsInteger() {
        // Test String to Integer conversion
        UserContext.setUserId("999");
        assertEquals("User ID should be converted to Integer", Integer.valueOf(999), UserContext.getUserIdAsInteger());

        // Test null conversion
        UserContext.clear();
        assertNull("Integer user ID should be null when not set", UserContext.getUserIdAsInteger());

        // Test invalid number format
        UserContext.setUserId("not_a_number");
        assertNull("Integer user ID should be null for invalid format", UserContext.getUserIdAsInteger());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyUserId() {
        // Should throw exception for empty string
        UserContext.setUserId("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetWhitespaceUserId() {
        // Should throw exception for whitespace-only string
        UserContext.setUserId("   ");
    }

    @Test
    public void testClearUserId() {
        // Test clearing
        UserContext.setUserId("123");
        assertNotNull("User ID should be set", UserContext.getUserId());

        UserContext.clear();
        assertNull("User ID should be cleared", UserContext.getUserId());
    }

    @Test
    public void testIsSet() {
        // Test isSet method
        assertFalse("isSet should be false initially", UserContext.isSet());

        UserContext.setUserId("123");
        assertTrue("isSet should be true after setting", UserContext.isSet());

        UserContext.clear();
        assertFalse("isSet should be false after clearing", UserContext.isSet());
    }

    @Test
    public void testGetUserIdOrDefault() {
        // Test with no user ID set
        assertEquals("Should return default", "default_user", UserContext.getUserIdOrDefault("default_user"));

        // Test with user ID set
        UserContext.setUserId("123");
        assertEquals("Should return set user ID", "123", UserContext.getUserIdOrDefault("default_user"));
    }

    @Test
    public void testGetDebugInfo() {
        // Test debug info
        UserContext.clear();
        String debugInfo = UserContext.getDebugInfo();
        assertTrue("Debug info should contain thread name", debugInfo.contains("Thread["));
        assertTrue("Debug info should show NOT_SET", debugInfo.contains("NOT_SET"));

        UserContext.setUserId("123");
        debugInfo = UserContext.getDebugInfo();
        assertTrue("Debug info should contain user ID", debugInfo.contains("123"));
    }

    @Test
    public void testThreadIsolation() throws InterruptedException {
        // Set user ID in main thread
        UserContext.setUserId("main_user");

        // Create another thread with different user ID
        Thread otherThread = new Thread(() -> {
            UserContext.setUserId("other_user");
            assertEquals("Other thread should have its own user ID", "other_user", UserContext.getUserId());
        });

        otherThread.start();
        otherThread.join();

        // Main thread should still have its original user ID
        assertEquals("Main thread user ID should not be affected", "main_user", UserContext.getUserId());
    }

    @Test
    public void testConcurrentMultiUserScenario() throws InterruptedException {
        // Simulate concurrent WebSocket requests from different users
        int numUsers = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numUsers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numUsers);
        List<String> errors = new CopyOnWriteArrayList<>();

        for (int i = 0; i < numUsers; i++) {
            final String userId = "user_" + i;
            executor.submit(() -> {
                try {
                    // Wait for all threads to be ready
                    startLatch.await();

                    // Simulate WebSocket request processing
                    UserContext.setUserId(userId);

                    // Simulate some processing time
                    Thread.sleep(10);

                    // Verify user ID is still correct
                    String retrievedUserId = UserContext.getUserId();
                    if (!userId.equals(retrievedUserId)) {
                        errors.add("Expected " + userId + " but got " + retrievedUserId);
                    }

                    // Simulate cleanup
                    UserContext.clear();

                } catch (Exception e) {
                    errors.add("Exception in thread: " + e.getMessage());
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();

        // Wait for all threads to complete
        boolean completed = completionLatch.await(5, TimeUnit.SECONDS);

        executor.shutdown();

        assertTrue("All threads should complete", completed);
        assertTrue("No errors should occur: " + errors, errors.isEmpty());
    }

    @Test
    public void testMemoryLeakPrevention() throws InterruptedException {
        // Test that ThreadLocal is properly cleaned up
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            final String userId = "user_" + i;
            futures.add(executor.submit(() -> {
                try {
                    UserContext.setUserId(userId);
                    // Simulate processing
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    // Critical: Always clear to prevent memory leaks
                    UserContext.clear();
                }
            }));
        }

        // Wait for all tasks to complete
        for (Future<?> future : futures) {
            try {
                future.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                fail("Task failed: " + e.getMessage());
            }
        }

        executor.shutdown();
        assertTrue("Executor should terminate", executor.awaitTermination(5, TimeUnit.SECONDS));

        // All ThreadLocals should be cleaned up
        // This test primarily validates that clear() is called properly
        // Memory leak detection would require heap analysis tools
    }

    @Test
    public void testWebSocketSimulation() throws InterruptedException {
        // Simulate realistic WebSocket usage pattern
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);
        List<String> results = new CopyOnWriteArrayList<>();

        // Simulate 3 concurrent WebSocket sessions
        String[] userIds = {"100", "200", "300"};
        for (String userId : userIds) {
            executor.submit(() -> {
                try {
                    // Simulate receiving WebSocket message
                    UserContext.setUserId(userId);

                    // Simulate expression parsing in Data class
                    String contextUserId = UserContext.getUserId();
                    results.add(contextUserId);

                    // Verify correct user ID
                    assertEquals("User ID should match in WebSocket context", userId, contextUserId);

                } finally {
                    // Simulate finally block cleanup
                    UserContext.clear();
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Verify all user IDs were processed correctly
        assertEquals("Should process all users", 3, results.size());
        assertTrue("Should contain user 100", results.contains("100"));
        assertTrue("Should contain user 200", results.contains("200"));
        assertTrue("Should contain user 300", results.contains("300"));
    }

    @Test
    public void testNestedContextUsage() {
        // Test that nested try-finally blocks work correctly
        UserContext.setUserId("outer");

        try {
            assertEquals("Outer context", "outer", UserContext.getUserId());

            // Nested operation
            try {
                UserContext.setUserId("inner");
                assertEquals("Inner context", "inner", UserContext.getUserId());
            } finally {
                UserContext.clear();
            }

            // After inner clear, outer context is also cleared (expected behavior)
            assertNull("Context should be cleared", UserContext.getUserId());

        } finally {
            UserContext.clear();
        }
    }

    @Test
    public void testExceptionDuringProcessing() {
        // Test that context is cleared even when exception occurs
        try {
            UserContext.setUserId("123");
            // Simulate exception during processing
            throw new RuntimeException("Simulated error");
        } catch (RuntimeException e) {
            // Expected
        } finally {
            UserContext.clear();
        }

        assertNull("Context should be cleared after exception", UserContext.getUserId());
    }

    @Test
    public void testRapidContextSwitching() throws InterruptedException, ExecutionException, TimeoutException {
        // Test rapid set/clear cycles
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Boolean> future = executor.submit(() -> {
            for (int i = 0; i < 1000; i++) {
                try {
                    UserContext.setUserId("user_" + i);
                    String retrieved = UserContext.getUserId();
                    if (!("user_" + i).equals(retrieved)) {
                        return false;
                    }
                } finally {
                    UserContext.clear();
                }
            }
            return true;
        });

        assertTrue("Rapid switching should work correctly", future.get(10, TimeUnit.SECONDS));
        executor.shutdown();
    }
}
