package com.ncslab.block.string;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import org.json.JSONObject;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for ToString block.
 * Tests converting numeric values to string representation.
 *
 * @author NCSLab Team
 * @version 1.0
 */
public class ToStringTest extends DirectBlockTestBase {

    private ToString toStringBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create ToString block
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "ToString");
        blockJson.put("blockName", "testToString");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        toStringBlock = new ToString(blockJson, mockModel);
        return toStringBlock;
    }

    @Test
    public void testIntegerToString() {
        // Test: Convert integer to string
        setScalarInput(0, 123.0);

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertTrue("Should convert 123 to string", output.contains("123"));
    }

    @Test
    public void testFloatingPointToString() {
        // Test: Convert floating point to string
        setScalarInput(0, 123.456);

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertTrue("Should contain numeric representation", output.length() > 0);
        assertTrue("Should contain digits", output.matches(".*\\d+.*"));
    }

    @Test
    public void testNegativeNumber() {
        // Test: Convert negative number
        setScalarInput(0, -45.67);

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertTrue("Should contain minus sign", output.contains("-"));
        assertTrue("Should contain digits", output.matches(".*\\d+.*"));
    }

    @Test
    public void testZero() {
        // Test: Convert zero
        setScalarInput(0, 0.0);

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertTrue("Should convert 0 to string", output.contains("0"));
    }

    @Test
    public void testVeryLargeNumber() {
        // Test: Convert very large number
        setScalarInput(0, 1.23e10);

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertNotNull("Output should not be null", output);
        assertTrue("Should contain numeric representation", output.length() > 0);
    }

    @Test
    public void testVerySmallNumber() {
        // Test: Convert very small number
        setScalarInput(0, 1.23e-10);

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertNotNull("Output should not be null", output);
        assertTrue("Should contain numeric representation", output.length() > 0);
    }

    @Test
    public void testNaN() {
        // Test: Convert NaN
        setScalarInput(0, Double.NaN);

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertTrue("Should handle NaN", output != null && output.length() > 0);
    }

    @Test
    public void testInfinity() {
        // Test: Convert positive infinity
        setScalarInput(0, Double.POSITIVE_INFINITY);

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertNotNull("Should handle infinity", output);
        assertTrue("Should contain infinity representation", output.length() > 0);
    }

    @Test
    public void testNegativeInfinity() {
        // Test: Convert negative infinity
        setScalarInput(0, Double.NEGATIVE_INFINITY);

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertNotNull("Should handle negative infinity", output);
        assertTrue("Should contain representation", output.length() > 0);
    }

    @Test
    public void testMultipleConversions() {
        // Test: Multiple numeric-to-string conversions
        double[] testCases = {0.0, 1.0, -1.0, 123.45, -678.9, 1e5, 1e-5};

        for (double testCase : testCases) {
            setScalarInput(0, testCase);

            block.calculateOutput(0.0);

            String output = getStringOutput(0);
            assertNotNull(String.format("Converting %f to string", testCase), output);
            assertTrue("Output should have content", output.length() > 0);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 123.456);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average ToString calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 20 microseconds on average
        assertTrue("ToString calculateOutput should be fast", avgTime < 20000);
    }
}
