package com.ncslab.block.string;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import org.json.JSONObject;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for StringTrim block.
 * Tests removing leading and trailing whitespace from strings.
 *
 * @author NCSLab Team
 * @version 1.0
 */
public class StringTrimTest extends DirectBlockTestBase {

    private StringTrim stringTrimBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create StringTrim block
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "StringTrim");
        blockJson.put("blockName", "testStringTrim");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        stringTrimBlock = new StringTrim(blockJson, mockModel);
        return stringTrimBlock;
    }

    @Test
    public void testBasicTrim() {
        // Test: Remove leading and trailing spaces
        setStringInput(0, "  Hello World  ");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should trim spaces", "Hello World", output);
    }

    @Test
    public void testLeadingSpacesOnly() {
        // Test: Remove only leading spaces
        setStringInput(0, "   Hello");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should trim leading spaces", "Hello", output);
    }

    @Test
    public void testTrailingSpacesOnly() {
        // Test: Remove only trailing spaces
        setStringInput(0, "World   ");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should trim trailing spaces", "World", output);
    }

    @Test
    public void testNoSpaces() {
        // Test: String with no leading/trailing spaces
        setStringInput(0, "Hello");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should remain unchanged", "Hello", output);
    }

    @Test
    public void testOnlySpaces() {
        // Test: String with only spaces
        setStringInput(0, "     ");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should become empty string", "", output);
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
    public void testInternalSpacesPreserved() {
        // Test: Internal spaces should be preserved
        setStringInput(0, "  Hello  World  ");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Internal spaces should be preserved", "Hello  World", output);
    }

    @Test
    public void testTabsAndNewlines() {
        // Test: Trim tabs and newlines
        setStringInput(0, "\t\nHello\t\n");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should trim tabs and newlines", "Hello", output);
    }

    @Test
    public void testMixedWhitespace() {
        // Test: Mixed whitespace characters
        setStringInput(0, " \t\n Hello World \n\t ");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should trim all whitespace", "Hello World", output);
    }

    @Test
    public void testMultipleTrimOperations() {
        // Test: Multiple trim operations
        String[][] testCases = {
            {"  Test  ", "Test"},
            {"NoSpaces", "NoSpaces"},
            {"   ", ""},
            {"\tTabbed\t", "Tabbed"},
            {"  Multi  Word  String  ", "Multi  Word  String"}
        };

        for (String[] testCase : testCases) {
            setStringInput(0, testCase[0]);

            block.calculateOutput(0.0);

            String expectedOutput = testCase[1];
            String actualOutput = getStringOutput(0);
            assertEquals(String.format("Trimming '%s'", testCase[0]),
                expectedOutput, actualOutput);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setStringInput(0, "     Test String     ");

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average StringTrim calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 15 microseconds on average
        assertTrue("StringTrim calculateOutput should be fast", avgTime < 15000);
    }
}
