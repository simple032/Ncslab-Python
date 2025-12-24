package com.ncslab.block.string;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import org.json.JSONObject;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for StringLower block.
 * Tests converting strings to lowercase.
 *
 * @author NCSLab Team
 * @version 1.0
 */
public class StringLowerTest extends DirectBlockTestBase {

    private StringLower stringLowerBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create StringLower block
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "StringLower");
        blockJson.put("blockName", "testStringLower");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        stringLowerBlock = new StringLower(blockJson, mockModel);
        return stringLowerBlock;
    }

    @Test
    public void testBasicLowercase() {
        // Test: Convert uppercase to lowercase
        setStringInput(0, "HELLO WORLD");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should convert to lowercase", "hello world", output);
    }

    @Test
    public void testMixedCase() {
        // Test: Mixed case conversion
        setStringInput(0, "HeLLo WoRLd");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should convert mixed case to lowercase", "hello world", output);
    }

    @Test
    public void testAlreadyLowercase() {
        // Test: Already lowercase string
        setStringInput(0, "hello world");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Lowercase should remain lowercase", "hello world", output);
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
        setStringInput(0, "ABC123!@#");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Numbers and symbols unchanged", "abc123!@#", output);
    }

    @Test
    public void testSingleCharacter() {
        // Test: Single uppercase character
        setStringInput(0, "A");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Single char should convert", "a", output);
    }

    @Test
    public void testWithSpaces() {
        // Test: String with spaces
        setStringInput(0, "  UPPER CASE  ");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should preserve spaces", "  upper case  ", output);
    }

    @Test
    public void testSpecialCharacters() {
        // Test: Special characters preserved
        setStringInput(0, "HELLO\nWORLD\tTEST");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Special chars preserved", "hello\nworld\ttest", output);
    }

    @Test
    public void testMultipleLowercaseOperations() {
        // Test: Multiple lowercase operations
        String[][] testCases = {
            {"UPPER", "upper"},
            {"MiXeD", "mixed"},
            {"lower", "lower"},
            {"123ABC", "123abc"},
            {"", ""}
        };

        for (String[] testCase : testCases) {
            setStringInput(0, testCase[0]);

            block.calculateOutput(0.0);

            String expectedOutput = testCase[1];
            String actualOutput = getStringOutput(0);
            assertEquals(String.format("Lowercasing '%s'", testCase[0]),
                expectedOutput, actualOutput);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setStringInput(0, "THIS IS A TEST STRING FOR PERFORMANCE MEASUREMENT");

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average StringLower calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 15 microseconds on average
        assertTrue("StringLower calculateOutput should be fast", avgTime < 15000);
    }
}
