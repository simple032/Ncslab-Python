package com.ncslab.block.string;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import org.json.JSONObject;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for StringFind block.
 * Tests finding substring position (index) within a string.
 *
 * @author NCSLab Team
 * @version 1.0
 */
public class StringFindTest extends DirectBlockTestBase {

    private StringFind stringFindBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create StringFind block
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "StringFind");
        blockJson.put("blockName", "testStringFind");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        stringFindBlock = new StringFind(blockJson, mockModel);
        return stringFindBlock;
    }

    @Test
    public void testFindSubstringAtBeginning() {
        // Test: Find substring at position 0 (beginning)
        setStringInput(0, "Hello World");
        setStringInput(1, "Hello");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should find 'Hello' at position 0", 0.0, output, DELTA);
    }

    @Test
    public void testFindSubstringInMiddle() {
        // Test: Find substring in middle
        setStringInput(0, "Hello World");
        setStringInput(1, "World");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should find 'World' at position 6", 6.0, output, DELTA);
    }

    @Test
    public void testSubstringNotFound() {
        // Test: Substring not found should return -1
        setStringInput(0, "Hello World");
        setStringInput(1, "Java");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should return -1 when substring not found", -1.0, output, DELTA);
    }

    @Test
    public void testCaseSensitiveFind() {
        // Test: Case-sensitive search
        setStringInput(0, "Hello World");
        setStringInput(1, "world");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Case-sensitive: 'world' not found in 'Hello World'", -1.0, output, DELTA);
    }

    @Test
    public void testEmptySubstring() {
        // Test: Empty substring should be found at position 0
        setStringInput(0, "Hello World");
        setStringInput(1, "");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Empty substring should be found at position 0", 0.0, output, DELTA);
    }

    @Test
    public void testEmptyString() {
        // Test: Empty string with empty substring
        setStringInput(0, "");
        setStringInput(1, "");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Empty in empty should be at position 0", 0.0, output, DELTA);
    }

    @Test
    public void testEmptyStringNonEmptySubstring() {
        // Test: Empty string with non-empty substring
        setStringInput(0, "");
        setStringInput(1, "Test");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Non-empty substring not found in empty string", -1.0, output, DELTA);
    }

    @Test
    public void testSingleCharacterFind() {
        // Test: Find single character
        setStringInput(0, "Hello");
        setStringInput(1, "e");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should find 'e' at position 1", 1.0, output, DELTA);
    }

    @Test
    public void testFindAtEnd() {
        // Test: Find substring at end of string
        setStringInput(0, "Testing");
        setStringInput(1, "ing");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should find 'ing' at position 4", 4.0, output, DELTA);
    }

    @Test
    public void testMultipleOccurrences() {
        // Test: Multiple occurrences should return first index
        setStringInput(0, "Test Test Test");
        setStringInput(1, "Test");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should return first occurrence at position 0", 0.0, output, DELTA);
    }

    @Test
    public void testSpecialCharacterFind() {
        // Test: Find special characters
        setStringInput(0, "Line1\nLine2");
        setStringInput(1, "\n");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should find newline at position 5", 5.0, output, DELTA);
    }

    @Test
    public void testUnicodeFind() {
        // Test: Find Unicode substring
        setStringInput(0, "Hello 世界");
        setStringInput(1, "世界");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should find Unicode substring at position 6", 6.0, output, DELTA);
    }

    @Test
    public void testNumericFind() {
        // Test: Find numeric substring
        setStringInput(0, "ID123456");
        setStringInput(1, "123");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should find '123' at position 2", 2.0, output, DELTA);
    }

    @Test
    public void testSubstringLongerThanString() {
        // Test: Substring longer than string
        setStringInput(0, "Short");
        setStringInput(1, "Very Long Substring");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Longer substring should not be found", -1.0, output, DELTA);
    }

    @Test
    public void testExactMatch() {
        // Test: Exact match (substring equals string)
        setStringInput(0, "Test");
        setStringInput(1, "Test");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Exact match should be found at position 0", 0.0, output, DELTA);
    }

    @Test
    public void testMultipleFindOperations() {
        // Test: Multiple find operations
        Object[][] testCases = {
            {"Hello World", "World", 6.0},
            {"Hello World", "world", -1.0},  // Case-sensitive
            {"Test123", "123", 4.0},
            {"ABC", "XYZ", -1.0},
            {"Test Test", "Test", 0.0}  // First occurrence
        };

        for (Object[] testCase : testCases) {
            setStringInput(0, (String) testCase[0]);
            setStringInput(1, (String) testCase[1]);

            block.calculateOutput(0.0);

            double expectedOutput = (Double) testCase[2];
            double actualOutput = getScalarOutput(0);
            assertEquals(String.format("Finding '%s' in '%s'", testCase[1], testCase[0]),
                expectedOutput, actualOutput, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setStringInput(0, "This is a test string for performance measurement");
        setStringInput(1, "performance");

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average StringFind calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 20 microseconds on average
        assertTrue("StringFind calculateOutput should be fast", avgTime < 20000);
    }
}
