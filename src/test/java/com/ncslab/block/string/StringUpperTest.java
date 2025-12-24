package com.ncslab.block.string;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import org.json.JSONObject;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for StringUpper block.
 * Tests converting strings to uppercase.
 *
 * @author NCSLab Team
 * @version 1.0
 */
public class StringUpperTest extends DirectBlockTestBase {

    private StringUpper stringUpperBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create StringUpper block
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "StringUpper");
        blockJson.put("blockName", "testStringUpper");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        stringUpperBlock = new StringUpper(blockJson, mockModel);
        return stringUpperBlock;
    }

    @Test
    public void testBasicUppercase() {
        // Test: Convert lowercase to uppercase
        setStringInput(0, "hello world");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should convert to uppercase", "HELLO WORLD", output);
    }

    @Test
    public void testMixedCase() {
        // Test: Mixed case conversion
        setStringInput(0, "HeLLo WoRLd");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should convert mixed case to uppercase", "HELLO WORLD", output);
    }

    @Test
    public void testAlreadyUppercase() {
        // Test: Already uppercase string
        setStringInput(0, "HELLO WORLD");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Uppercase should remain uppercase", "HELLO WORLD", output);
    }

    @Test
    public void testEmptyString() {
        // Test: Empty string
        setStringInput(0, "");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Empty string should remain empty", "", output);
    }

    @Test
    public void testNumbersAndSymbols() {
        // Test: Numbers and symbols (should remain unchanged)
        setStringInput(0, "abc123!@#");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Numbers and symbols unchanged", "ABC123!@#", output);
    }

    @Test
    public void testSingleCharacter() {
        // Test: Single lowercase character
        setStringInput(0, "a");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Single char should convert", "A", output);
    }

    @Test
    public void testWithSpaces() {
        // Test: String with spaces
        setStringInput(0, "  lower case  ");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should preserve spaces", "  LOWER CASE  ", output);
    }

    @Test
    public void testSpecialCharacters() {
        // Test: Special characters preserved
        setStringInput(0, "hello\nworld\ttest");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Special chars preserved", "HELLO\nWORLD\tTEST", output);
    }

    @Test
    public void testMultipleUppercaseOperations() {
        // Test: Multiple uppercase operations
        String[][] testCases = {
            {"lower", "LOWER"},
            {"MiXeD", "MIXED"},
            {"UPPER", "UPPER"},
            {"abc123", "ABC123"},
            {"", ""}
        };

        for (String[] testCase : testCases) {
            setStringInput(0, testCase[0]);

            block.calculateOutput(0.0);

            String expectedOutput = testCase[1];
            String actualOutput = getStringOutput(0);
            assertEquals(String.format("Uppercasing '%s'", testCase[0]),
                expectedOutput, actualOutput);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setStringInput(0, "this is a test string for performance measurement");

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average StringUpper calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 15 microseconds on average
        assertTrue("StringUpper calculateOutput should be fast", avgTime < 15000);
    }
}
