package com.ncslab.block.string;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import org.json.JSONObject;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for StringReplace block.
 * Tests replacing pattern occurrences in strings.
 *
 * @author NCSLab Team
 * @version 1.0
 */
public class StringReplaceTest extends DirectBlockTestBase {

    private StringReplace stringReplaceBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create StringReplace block
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "StringReplace");
        blockJson.put("blockName", "testStringReplace");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        stringReplaceBlock = new StringReplace(blockJson, mockModel);
        return stringReplaceBlock;
    }

    @Test
    public void testBasicReplacement() {
        // Test: Basic pattern replacement
        setStringInput(0, "Hello World");
        setStringInput(1, "World");
        setStringInput(2, "Java");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should replace 'World' with 'Java'", "Hello Java", output);
    }

    @Test
    public void testMultipleOccurrences() {
        // Test: Replace all occurrences
        setStringInput(0, "Test Test Test");
        setStringInput(1, "Test");
        setStringInput(2, "Pass");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should replace all occurrences", "Pass Pass Pass", output);
    }

    @Test
    public void testPatternNotFound() {
        // Test: Pattern not found
        setStringInput(0, "Hello World");
        setStringInput(1, "Java");
        setStringInput(2, "Python");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should return unchanged if pattern not found", "Hello World", output);
    }

    @Test
    public void testEmptyPattern() {
        // Test: Empty pattern (should not replace anything)
        setStringInput(0, "Hello World");
        setStringInput(1, "");
        setStringInput(2, "X");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        // Empty pattern behavior: typically inserts replacement between every character
        assertNotNull("Output should not be null", output);
    }

    @Test
    public void testEmptyReplacement() {
        // Test: Empty replacement (removes pattern)
        setStringInput(0, "Hello World");
        setStringInput(1, " ");
        setStringInput(2, "");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Empty replacement should remove pattern", "HelloWorld", output);
    }

    @Test
    public void testEmptyString() {
        // Test: Empty input string
        setStringInput(0, "");
        setStringInput(1, "Test");
        setStringInput(2, "Replace");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Empty string should remain empty", "", output);
    }

    @Test
    public void testReplaceAtBeginning() {
        // Test: Replace at beginning
        setStringInput(0, "Hello World");
        setStringInput(1, "Hello");
        setStringInput(2, "Hi");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should replace at beginning", "Hi World", output);
    }

    @Test
    public void testReplaceAtEnd() {
        // Test: Replace at end
        setStringInput(0, "Hello World");
        setStringInput(1, "World");
        setStringInput(2, "Earth");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should replace at end", "Hello Earth", output);
    }

    @Test
    public void testReplaceWithLongerString() {
        // Test: Replacement longer than pattern
        setStringInput(0, "Hi");
        setStringInput(1, "Hi");
        setStringInput(2, "Hello World");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should handle longer replacement", "Hello World", output);
    }

    @Test
    public void testReplaceWithShorterString() {
        // Test: Replacement shorter than pattern
        setStringInput(0, "Hello World");
        setStringInput(1, "Hello");
        setStringInput(2, "Hi");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should handle shorter replacement", "Hi World", output);
    }

    @Test
    public void testCaseSensitiveReplace() {
        // Test: Case-sensitive replacement
        setStringInput(0, "Hello hello HELLO");
        setStringInput(1, "hello");
        setStringInput(2, "hi");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should be case-sensitive", "Hello hi HELLO", output);
    }

    @Test
    public void testReplaceSpecialCharacters() {
        // Test: Replace special characters
        setStringInput(0, "Line1\nLine2");
        setStringInput(1, "\n");
        setStringInput(2, " ");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should replace newline with space", "Line1 Line2", output);
    }

    @Test
    public void testReplaceNumbers() {
        // Test: Replace numeric patterns
        setStringInput(0, "ID123456");
        setStringInput(1, "123");
        setStringInput(2, "999");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Should replace numbers", "ID999456", output);
    }

    @Test
    public void testMultipleReplaceOperations() {
        // Test: Multiple replace operations
        String[][] testCases = {
            {"Hello World", "World", "Java", "Hello Java"},
            {"Test Test", "Test", "Pass", "Pass Pass"},
            {"ABC", "XYZ", "123", "ABC"},  // Pattern not found
            {"Remove  spaces", "  ", " ", "Remove spaces"}
        };

        for (String[] testCase : testCases) {
            setStringInput(0, testCase[0]);
            setStringInput(1, testCase[1]);
            setStringInput(2, testCase[2]);

            block.calculateOutput(0.0);

            String expectedOutput = testCase[3];
            String actualOutput = getStringOutput(0);
            assertEquals(String.format("Replacing '%s' with '%s' in '%s'",
                testCase[1], testCase[2], testCase[0]),
                expectedOutput, actualOutput);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setStringInput(0, "This is a test string for performance measurement test");
        setStringInput(1, "test");
        setStringInput(2, "TEST");

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average StringReplace calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 25 microseconds on average
        assertTrue("StringReplace calculateOutput should be fast", avgTime < 25000);
    }
}
