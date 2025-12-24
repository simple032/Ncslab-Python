package com.ncslab.block.string;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import org.json.JSONObject;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for StringToDouble block.
 * Tests string-to-double numeric conversion.
 *
 * @author NCSLab Team
 * @version 1.0
 */
public class StringToDoubleTest extends DirectBlockTestBase {

    private StringToDouble stringToDoubleBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create StringToDouble block
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "StringToDouble");
        blockJson.put("blockName", "testStringToDouble");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        stringToDoubleBlock = new StringToDouble(blockJson, mockModel);
        return stringToDoubleBlock;
    }

    @Test
    public void testBasicIntegerConversion() {
        // Test: Basic integer string conversion
        setStringInput(0, "123");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should convert '123' to 123.0", 123.0, output, DELTA);
    }

    @Test
    public void testFloatingPointConversion() {
        // Test: Floating point conversion
        setStringInput(0, "123.456");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should convert '123.456' to 123.456", 123.456, output, DELTA);
    }

    @Test
    public void testNegativeNumber() {
        // Test: Negative number conversion
        setStringInput(0, "-45.67");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should convert '-45.67' to -45.67", -45.67, output, DELTA);
    }

    @Test
    public void testZeroConversion() {
        // Test: Zero conversion
        setStringInput(0, "0");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should convert '0' to 0.0", 0.0, output, DELTA);
    }

    @Test
    public void testScientificNotation() {
        // Test: Scientific notation
        setStringInput(0, "1.23e5");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should convert '1.23e5' to 123000.0", 123000.0, output, DELTA);
    }

    @Test
    public void testNegativeScientificNotation() {
        // Test: Negative exponent scientific notation
        setStringInput(0, "1.5e-3");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should convert '1.5e-3' to 0.0015", 0.0015, output, 1e-10);
    }

    @Test
    public void testStringWithLeadingSpaces() {
        // Test: String with leading spaces (should trim)
        setStringInput(0, "   456.78");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should trim and convert '   456.78'", 456.78, output, DELTA);
    }

    @Test
    public void testStringWithTrailingSpaces() {
        // Test: String with trailing spaces (should trim)
        setStringInput(0, "789.12   ");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should trim and convert '789.12   '", 789.12, output, DELTA);
    }

    @Test
    public void testInvalidString() {
        // Test: Invalid string should return NaN
        setStringInput(0, "NotANumber");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertTrue("Invalid string should return NaN", Double.isNaN(output));
    }

    @Test
    public void testEmptyString() {
        // Test: Empty string should return NaN
        setStringInput(0, "");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertTrue("Empty string should return NaN", Double.isNaN(output));
    }

    @Test
    public void testOnlyWhitespace() {
        // Test: Only whitespace should return NaN
        setStringInput(0, "   ");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertTrue("Whitespace-only string should return NaN", Double.isNaN(output));
    }

    @Test
    public void testPartiallyValidString() {
        // Test: Partially valid string (number followed by text)
        setStringInput(0, "123abc");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertTrue("Partially valid string should return NaN", Double.isNaN(output));
    }

    @Test
    public void testInfinity() {
        // Test: String "Infinity"
        setStringInput(0, "Infinity");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertTrue("'Infinity' should convert to positive infinity",
            Double.isInfinite(output) && output > 0);
    }

    @Test
    public void testNegativeInfinity() {
        // Test: String "-Infinity"
        setStringInput(0, "-Infinity");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertTrue("'-Infinity' should convert to negative infinity",
            Double.isInfinite(output) && output < 0);
    }

    @Test
    public void testNaNString() {
        // Test: String "NaN"
        setStringInput(0, "NaN");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertTrue("'NaN' string should convert to NaN", Double.isNaN(output));
    }

    @Test
    public void testVeryLargeNumber() {
        // Test: Very large number
        setStringInput(0, "1.7976931348623157e308");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should handle very large numbers", 1.7976931348623157e308, output, 1e293);
    }

    @Test
    public void testVerySmallNumber() {
        // Test: Very small number
        setStringInput(0, "4.9e-324");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertTrue("Should handle very small numbers", output > 0 && output < 1e-300);
    }

    @Test
    public void testMultipleConversions() {
        // Test: Multiple conversion operations
        Object[][] testCases = {
            {"123", 123.0, false},
            {"45.67", 45.67, false},
            {"-89.12", -89.12, false},
            {"1.5e3", 1500.0, false},
            {"invalid", Double.NaN, true},  // Check NaN separately
            {"0", 0.0, false}
        };

        for (Object[] testCase : testCases) {
            setStringInput(0, (String) testCase[0]);

            block.calculateOutput(0.0);

            double expectedOutput = (Double) testCase[1];
            boolean isNaN = (Boolean) testCase[2];
            double actualOutput = getScalarOutput(0);

            if (isNaN) {
                assertTrue(String.format("Converting '%s' should yield NaN", testCase[0]),
                    Double.isNaN(actualOutput));
            } else {
                assertEquals(String.format("Converting '%s'", testCase[0]),
                    expectedOutput, actualOutput, DELTA);
            }
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setStringInput(0, "123.456");

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average StringToDouble calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 20 microseconds on average
        assertTrue("StringToDouble calculateOutput should be fast", avgTime < 20000);
    }
}
