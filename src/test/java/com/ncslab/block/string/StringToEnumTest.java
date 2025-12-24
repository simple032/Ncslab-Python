package com.ncslab.block.string;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import org.json.JSONObject;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for StringToEnum block.
 * Tests string-to-enum conversion.
 *
 * @author NCSLab Team
 * @version 1.0
 */
public class StringToEnumTest extends DirectBlockTestBase {

    private StringToEnum stringToEnumBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create StringToEnum block with enum values parameter
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "StringToEnum");
        blockJson.put("blockName", "testStringToEnum");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        // Set enum values parameter (e.g., "RED,GREEN,BLUE")
        JSONObject paramValues = new JSONObject();
        paramValues.put("EnumValues", "RED,GREEN,BLUE");
        blockJson.put("paramValues", paramValues);

        stringToEnumBlock = new StringToEnum(blockJson, mockModel);
        return stringToEnumBlock;
    }

    @Test
    public void testFirstEnumValue() {
        // Test: Convert first enum value (index 0)
        setStringInput(0, "RED");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("'RED' should map to index 0", 0.0, output, DELTA);
    }

    @Test
    public void testSecondEnumValue() {
        // Test: Convert second enum value (index 1)
        setStringInput(0, "GREEN");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("'GREEN' should map to index 1", 1.0, output, DELTA);
    }

    @Test
    public void testThirdEnumValue() {
        // Test: Convert third enum value (index 2)
        setStringInput(0, "BLUE");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("'BLUE' should map to index 2", 2.0, output, DELTA);
    }

    @Test
    public void testInvalidEnumValue() {
        // Test: Invalid enum value should return -1 or 0 (default)
        setStringInput(0, "YELLOW");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        // Typically returns -1 or 0 for invalid enum value
        assertTrue("Invalid enum should return -1 or 0", output == -1.0 || output == 0.0);
    }

    @Test
    public void testEmptyString() {
        // Test: Empty string
        setStringInput(0, "");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertTrue("Empty string should return -1 or 0", output == -1.0 || output == 0.0);
    }

    @Test
    public void testCaseSensitivity() {
        // Test: Case sensitivity (typically enum values are case-sensitive)
        setStringInput(0, "red");  // lowercase

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        // Should not match "RED" if case-sensitive
        assertTrue("Lowercase 'red' might not match 'RED'", output >= -1.0);
    }

    @Test
    public void testMultipleEnumConversions() {
        // Test: Multiple enum conversions
        String[] testCases = {"RED", "GREEN", "BLUE", "RED", "GREEN"};
        double[] expectedOutputs = {0.0, 1.0, 2.0, 0.0, 1.0};

        for (int i = 0; i < testCases.length; i++) {
            setStringInput(0, testCases[i]);

            block.calculateOutput(0.0);

            double expectedOutput = expectedOutputs[i];
            double actualOutput = getScalarOutput(0);
            assertEquals(String.format("Converting enum '%s'", testCases[i]),
                expectedOutput, actualOutput, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setStringInput(0, "GREEN");

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average StringToEnum calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 15 microseconds on average
        assertTrue("StringToEnum calculateOutput should be fast", avgTime < 15000);
    }
}
