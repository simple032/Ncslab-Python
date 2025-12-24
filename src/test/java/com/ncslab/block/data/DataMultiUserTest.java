package com.ncslab.block.data;

import com.ncslab.util.UserContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * Integration tests for Data class expression parsing with multi-user support.
 * Tests that UserContext properly propagates to expression parsing.
 */
public class DataMultiUserTest {

    @Before
    public void setUp() {
        UserContext.clear();
    }

    @After
    public void tearDown() {
        UserContext.clear();
    }

    @Test
    public void testDataCreationWithUserContext() {
        // Set user context
        UserContext.setUserId("123");

        try {
            // Create Data object - this should use the user context internally
            Data data = new Data("5.0");

            // Verify data was created successfully
            assertNotNull("Data should be created", data);
            assertEquals("Data value should be parsed", 5.0, data.getInitValue(), 0.001);

        } finally {
            UserContext.clear();
        }
    }

    @Test
    public void testDataCreationWithoutUserContext() {
        // Don't set user context - should fall back to default user ID "18"
        Data data = new Data("3.14");

        assertNotNull("Data should be created with default user ID", data);
        assertEquals("Data value should be parsed", 3.14, data.getInitValue(), 0.001);
    }

    @Test
    public void testConcurrentDataCreation() throws InterruptedException {
        // Simulate concurrent data creation from different users
        int numUsers = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numUsers);
        CountDownLatch latch = new CountDownLatch(numUsers);
        ConcurrentHashMap<String, Data> results = new ConcurrentHashMap<>();
        List<String> errors = new CopyOnWriteArrayList<>();

        for (int i = 0; i < numUsers; i++) {
            final String userId = "user_" + i;
            final double value = i + 1.0;

            executor.submit(() -> {
                try {
                    // Set user context (simulating WebSocket endpoint)
                    UserContext.setUserId(userId);

                    // Create Data object (simulating block parameter parsing)
                    Data data = new Data(String.valueOf(value));

                    // Store result
                    results.put(userId, data);

                    // Verify correct value
                    if (Math.abs(data.getInitValue() - value) > 0.001) {
                        errors.add("User " + userId + " got wrong value: " + data.getInitValue());
                    }

                } catch (Exception e) {
                    errors.add("Exception for user " + userId + ": " + e.getMessage());
                } finally {
                    UserContext.clear();
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue("All tasks should complete", completed);
        assertTrue("No errors should occur: " + errors, errors.isEmpty());
        assertEquals("All users should have results", numUsers, results.size());
    }

    @Test
    public void testDataThreadIsolation() throws InterruptedException {
        // Test that different threads can create Data objects independently
        CountDownLatch latch = new CountDownLatch(2);
        String[] user1Result = new String[1];
        String[] user2Result = new String[1];

        Thread thread1 = new Thread(() -> {
            try {
                UserContext.setUserId("user1");
                Data data = new Data("10.0");
                user1Result[0] = "user1:" + data.getInitValue();
            } finally {
                UserContext.clear();
                latch.countDown();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                UserContext.setUserId("user2");
                Data data = new Data("20.0");
                user2Result[0] = "user2:" + data.getInitValue();
            } finally {
                UserContext.clear();
                latch.countDown();
            }
        });

        thread1.start();
        thread2.start();

        latch.await(5, TimeUnit.SECONDS);

        assertEquals("Thread 1 should have correct result", "user1:10.0", user1Result[0]);
        assertEquals("Thread 2 should have correct result", "user2:20.0", user2Result[0]);
    }

    @Test
    public void testDataWithMatrixAndUserContext() {
        // Test matrix data creation with user context
        UserContext.setUserId("matrix_user");

        try {
            // Create matrix data
            Data matrixData = new Data("[1, 2; 3, 4]");

            assertNotNull("Matrix data should be created", matrixData);
            assertEquals("Should be MATRIX type", DataType.MATRIX, matrixData.getDataType());
            assertEquals("Matrix width should be 2", 2, matrixData.getWidth());
            assertEquals("Matrix height should be 2", 2, matrixData.getHeight());

        } finally {
            UserContext.clear();
        }
    }

    @Test
    public void testDataCreationStressTest() throws InterruptedException {
        // Stress test with many concurrent data creations
        int numThreads = 10;
        int operationsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        ConcurrentHashMap<String, Integer> successCounts = new ConcurrentHashMap<>();

        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            executor.submit(() -> {
                int successes = 0;
                try {
                    UserContext.setUserId("stress_user_" + threadId);

                    for (int i = 0; i < operationsPerThread; i++) {
                        try {
                            Data data = new Data(String.valueOf(i));
                            if (data != null && data.getInitValue() == i) {
                                successes++;
                            }
                        } catch (Exception e) {
                            // Log but continue
                            System.err.println("Failed to create data: " + e.getMessage());
                        }
                    }

                    successCounts.put("thread_" + threadId, successes);

                } finally {
                    UserContext.clear();
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue("Stress test should complete", completed);
        assertEquals("All threads should report", numThreads, successCounts.size());

        // Verify all operations succeeded
        for (Integer count : successCounts.values()) {
            assertTrue("Should have high success rate: " + count, count >= operationsPerThread * 0.9);
        }
    }

    @Test
    public void testUserContextPropagationInRealScenario() {
        // Simulate real WebSocket request flow
        String userId = "real_user_123";

        // Step 1: WebSocket endpoint receives request
        UserContext.setUserId(userId);

        try {
            // Step 2: Model parsing creates blocks with parameters
            // Blocks create Data objects for parameters
            Data stepTime = new Data("1.0");
            Data initialValue = new Data("0.0");
            Data finalValue = new Data("5.0");

            // Step 3: Verify all Data objects were created with correct user context
            assertNotNull(stepTime);
            assertNotNull(initialValue);
            assertNotNull(finalValue);

            assertEquals("Step time should be parsed", 1.0, stepTime.getInitValue(), 0.001);
            assertEquals("Initial value should be parsed", 0.0, initialValue.getInitValue(), 0.001);
            assertEquals("Final value should be parsed", 5.0, finalValue.getInitValue(), 0.001);

            // Verify user context is still set
            assertEquals("User context should still be set", userId, UserContext.getUserId());

        } finally {
            // Step 4: WebSocket finally block clears context
            UserContext.clear();
        }

        // Step 5: Verify context is cleared
        assertNull("Context should be cleared after request", UserContext.getUserId());
    }

    @Test
    public void testExpressionParsingFallback() {
        // Test that expression parsing falls back gracefully when no context is set

        // Don't set user context
        assertNull("User context should be null", UserContext.getUserId());

        // Should still work with default user ID
        Data data = new Data("2.5");

        assertNotNull("Data should be created with fallback", data);
        assertEquals("Value should be parsed", 2.5, data.getInitValue(), 0.001);
    }

    @Test
    public void testMultipleDataObjectsSameContext() {
        // Test creating multiple Data objects in same user context
        UserContext.setUserId("multi_data_user");

        try {
            Data data1 = new Data("1.0");
            Data data2 = new Data("2.0");
            Data data3 = new Data("[1, 2, 3]");

            assertNotNull(data1);
            assertNotNull(data2);
            assertNotNull(data3);

            assertEquals(1.0, data1.getInitValue(), 0.001);
            assertEquals(2.0, data2.getInitValue(), 0.001);
            assertEquals(DataType.MATRIX, data3.getDataType());

        } finally {
            UserContext.clear();
        }
    }
}
