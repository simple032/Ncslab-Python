package com.ncslab.block.string;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import org.json.JSONObject;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for StringContains block.
 * Tests substring detection with case-sensitive matching.
 *
 * @author NCSLab Team
 * @version 1.0
 */
public class StringContainsTest extends DirectBlockTestBase {

    private StringContains stringContainsBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create StringContains block
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "StringContains");
        blockJson.put("blockName", "testStringContains");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        stringContainsBlock = new StringContains(blockJson, mockModel);
        return stringContainsBlock;
    }

    @Test
    public void testContainsSubstring() {
        // Test: String contains substring
        setStringInput(0, "Hello World");
        setStringInput(1, "World");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should find 'World' in 'Hello World'", 1.0, output, DELTA);
    }

    @Test
    public void testDoesNotContainSubstring() {
        // Test: String does not contain substring
        setStringInput(0, "Hello World");
        setStringInput(1, "Java");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should not find 'Java' in 'Hello World'", 0.0, output, DELTA);
    }

    @Test
    public void testCaseSensitivity() {
        // Test: Case-sensitive matching (default)
        setStringInput(0, "Hello World");
        setStringInput(1, "world");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Case-sensitive: should not find 'world' in 'Hello World'", 0.0, output, DELTA);
    }

    @Test
    public void testContainsAtBeginning() {
        // Test: Substring at beginning
        setStringInput(0, "Hello World");
        setStringInput(1, "Hello");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should find substring at beginning", 1.0, output, DELTA);
    }

    @Test
    public void testContainsAtEnd() {
        // Test: Substring at end
        setStringInput(0, "Hello World");
        setStringInput(1, "World");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should find substring at end", 1.0, output, DELTA);
    }

    @Test
    public void testContainsInMiddle() {
        // Test: Substring in middle
        setStringInput(0, "Hello World Test");
        setStringInput(1, "World");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should find substring in middle", 1.0, output, DELTA);
    }

    @Test
    public void testEmptySubstring() {
        // Test: Empty substring (should return true - every string contains empty string)
        setStringInput(0, "Hello World");
        setStringInput(1, "");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Empty substring should be found", 1.0, output, DELTA);
    }

    @Test
    public void testEmptyString() {
        // Test: Empty string contains only empty substring
        setStringInput(0, "");
        setStringInput(1, "");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Empty string contains empty substring", 1.0, output, DELTA);
    }

    @Test
    public void testEmptyStringNonEmptySubstring() {
        // Test: Empty string does not contain non-empty substring
        setStringInput(0, "");
        setStringInput(1, "Test");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Empty string should not contain non-empty substring", 0.0, output, DELTA);
    }

    @Test
    public void testSubstringLongerThanString() {
        // Test: Substring longer than string
        setStringInput(0, "Short");
        setStringInput(1, "Very Long Substring");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Longer substring should not be found", 0.0, output, DELTA);
    }

    @Test
    public void testSingleCharacterSubstring() {
        // Test: Single character substring
        setStringInput(0, "Hello");
        setStringInput(1, "e");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should find single character 'e'", 1.0, output, DELTA);
    }

    @Test
    public void testSpecialCharacters() {
        // Test: Special characters in substring
        setStringInput(0, "Line1\nLine2");
        setStringInput(1, "\n");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should find newline character", 1.0, output, DELTA);
    }

    @Test
    public void testUnicodeSubstring() {
        // Test: Unicode character substring
        setStringInput(0, "Hello 世界");
        setStringInput(1, "世界");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should find Unicode substring", 1.0, output, DELTA);
    }

    @Test
    public void testNumericSubstring() {
        // Test: Numeric substring
        setStringInput(0, "ID123456");
        setStringInput(1, "123");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should find numeric substring", 1.0, output, DELTA);
    }

    @Test
    public void testMultipleOccurrences() {
        // Test: Multiple occurrences (should still return 1.0 if found)
        setStringInput(0, "Test Test Test");
        setStringInput(1, "Test");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should find substring even with multiple occurrences", 1.0, output, DELTA);
    }

    @Test
    public void testPartialMatch() {
        // Test: Partial match should not be found
        setStringInput(0, "Testing");
        setStringInput(1, "Tested");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Partial match should not be found", 0.0, output, DELTA);
    }

    @Test
    public void testMultipleContainsChecks() {
        // Test: Multiple contains checks
        String[][] testCases = {
            {"Hello World", "World", "1.0"},
            {"Hello World", "world", "0.0"},  // Case-sensitive
            {"Test123", "123", "1.0"},
            {"ABC", "XYZ", "0.0"},
            {"", "", "1.0"}
        };

        for (String[] testCase : testCases) {
            setStringInput(0, testCase[0]);
            setStringInput(1, testCase[1]);

            block.calculateOutput(0.0);

            double expectedOutput = Double.parseDouble(testCase[2]);
            double actualOutput = getScalarOutput(0);
            assertEquals(String.format("'%s' contains '%s'", testCase[0], testCase[1]),
                expectedOutput, actualOutput, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setStringInput(0, "This is a test string for performance measurement");
        setStringInput(1, "performance");

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average StringContains calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 20 microseconds on average
        assertTrue("StringContains calculateOutput should be fast", avgTime < 20000);
    }
}
