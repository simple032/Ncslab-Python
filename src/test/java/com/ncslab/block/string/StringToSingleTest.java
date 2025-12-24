package com.ncslab.block.string;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import org.json.JSONObject;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for StringToSingle block.
 * Tests string-to-single (float) numeric conversion.
 *
 * @author NCSLab Team
 * @version 1.0
 */
public class StringToSingleTest extends DirectBlockTestBase {

    private StringToSingle stringToSingleBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create StringToSingle block
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "StringToSingle");
        blockJson.put("blockName", "testStringToSingle");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        stringToSingleBlock = new StringToSingle(blockJson, mockModel);
        return stringToSingleBlock;
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
        // Note: Single precision may have lower accuracy
        assertEquals("Should convert '123.456' to ~123.456", 123.456, output, 0.001);
    }

    @Test
    public void testNegativeNumber() {
        // Test: Negative number conversion
        setStringInput(0, "-45.67");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should convert '-45.67' to -45.67", -45.67, output, 0.001);
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
        assertEquals("Should convert '1.23e5' to 123000.0", 123000.0, output, 1.0);
    }

    @Test
    public void testStringWithSpaces() {
        // Test: String with leading/trailing spaces (should trim)
        setStringInput(0, "   456.78   ");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should trim and convert '   456.78   '", 456.78, output, 0.001);
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
    public void testSinglePrecisionLimits() {
        // Test: Single precision maximum value (approximately 3.4e38)
        setStringInput(0, "3.4e38");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertTrue("Should handle single precision maximum", output > 0);
    }

    @Test
    public void testPrecisionDifference() {
        // Test: Demonstrate precision difference between float and double
        // Single precision has about 7 decimal digits of precision
        setStringInput(0, "1.23456789");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        // Single precision may lose some precision
        assertEquals("Should convert with single precision", 1.23456789, output, 0.00001);
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
    public void testMultipleConversions() {
        // Test: Multiple conversion operations
        Object[][] testCases = {
            {"123", 123.0, false},
            {"45.67", 45.67, false},
            {"-89.1", -89.1, false},
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
                    expectedOutput, actualOutput, 0.01);
            }
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setStringInput(0, "123.456");

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average StringToSingle calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 20 microseconds on average
        assertTrue("StringToSingle calculateOutput should be fast", avgTime < 20000);
    }
}
