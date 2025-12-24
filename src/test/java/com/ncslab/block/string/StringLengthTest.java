package com.ncslab.block.string;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import org.json.JSONObject;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for StringLength block.
 * Tests string length calculation for various inputs.
 *
 * @author NCSLab Team
 * @version 1.0
 */
public class StringLengthTest extends DirectBlockTestBase {

    private StringLength stringLengthBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create StringLength block
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "StringLength");
        blockJson.put("blockName", "testStringLength");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        stringLengthBlock = new StringLength(blockJson, mockModel);
        return stringLengthBlock;
    }

    @Test
    public void testBasicStringLength() {
        // Test: Basic string length
        setStringInput(0, "Hello World");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Length of 'Hello World' should be 11", 11.0, output, DELTA);
    }

    @Test
    public void testEmptyStringLength() {
        // Test: Empty string length
        setStringInput(0, "");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Length of empty string should be 0", 0.0, output, DELTA);
    }

    @Test
    public void testSingleCharacterLength() {
        // Test: Single character
        setStringInput(0, "A");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Length of 'A' should be 1", 1.0, output, DELTA);
    }

    @Test
    public void testStringWithSpaces() {
        // Test: Spaces count toward length
        setStringInput(0, "   ");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Length of '   ' should be 3", 3.0, output, DELTA);
    }

    @Test
    public void testSpecialCharacters() {
        // Test: Special characters count as single characters
        setStringInput(0, "A\nB\tC");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Length of 'A\\nB\\tC' should be 5", 5.0, output, DELTA);
    }

    @Test
    public void testUnicodeCharacters() {
        // Test: Unicode characters count
        setStringInput(0, "Hello 世界");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Length of 'Hello 世界' should be 8", 8.0, output, DELTA);
    }

    @Test
    public void testNumericString() {
        // Test: Numeric string length
        setStringInput(0, "123456789");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Length of '123456789' should be 9", 9.0, output, DELTA);
    }

    @Test
    public void testVeryLongString() {
        // Test: Very long string
        StringBuilder longStr = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            longStr.append("X");
        }

        setStringInput(0, longStr.toString());

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Length of 500-char string should be 500", 500.0, output, DELTA);
    }

    @Test
    public void testMultipleLengthCalculations() {
        // Test: Multiple length calculations
        Object[][] testCases = {
            {"Test", 4.0},
            {"", 0.0},
            {"A", 1.0},
            {"1234567890", 10.0},
            {"Multi Word String", 17.0}
        };

        for (Object[] testCase : testCases) {
            setStringInput(0, (String) testCase[0]);

            block.calculateOutput(0.0);

            double expectedLength = (Double) testCase[1];
            double actualLength = getScalarOutput(0);
            assertEquals(String.format("Length of '%s'", testCase[0]),
                expectedLength, actualLength, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setStringInput(0, "This is a test string for performance measurement");

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average StringLength calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 10 microseconds on average
        assertTrue("StringLength calculateOutput should be fast", avgTime < 10000);
    }
}
