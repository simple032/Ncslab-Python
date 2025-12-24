package com.ncslab.block.string;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import org.json.JSONObject;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for StringCompare block.
 * Tests string comparison with case sensitivity options.
 *
 * @author NCSLab Team
 * @version 1.0
 */
public class StringCompareTest extends DirectBlockTestBase {

    private StringCompare stringCompareBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create StringCompare block with case-sensitive comparison (default)
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "StringCompare");
        blockJson.put("blockName", "testStringCompare");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        JSONObject paramValues = new JSONObject();
        paramValues.put("CaseSensitive", "on");
        blockJson.put("paramValues", paramValues);

        stringCompareBlock = new StringCompare(blockJson, mockModel);
        return stringCompareBlock;
    }

    @Test
    public void testEqualStrings() {
        // Test: Equal strings should return 1.0 (true)
        setStringInput(0, "Hello");
        setStringInput(1, "Hello");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Equal strings should return 1.0", 1.0, output, DELTA);
    }

    @Test
    public void testUnequalStrings() {
        // Test: Unequal strings should return 0.0 (false)
        setStringInput(0, "Hello");
        setStringInput(1, "World");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Unequal strings should return 0.0", 0.0, output, DELTA);
    }

    @Test
    public void testCaseSensitiveComparison() {
        // Test: Case-sensitive comparison (default: "on")
        setStringInput(0, "Hello");
        setStringInput(1, "hello");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Case-sensitive: 'Hello' != 'hello'", 0.0, output, DELTA);
    }

    @Test
    public void testCaseInsensitiveComparison() throws Exception {
        // Test: Case-insensitive comparison
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "StringCompare");
        blockJson.put("blockName", "testCaseInsensitive");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        JSONObject paramValues = new JSONObject();
        paramValues.put("CaseSensitive", "off");
        blockJson.put("paramValues", paramValues);

        StringCompare caseInsensitiveBlock = new StringCompare(blockJson, mockModel);
        initializeOutputSignals(caseInsensitiveBlock);
        caseInsensitiveBlock.calculateInit();

        setStringInput(0, "Hello");
        setStringInput(1, "hello");
        mockInputPortConnection(caseInsensitiveBlock, 0, inputDataMap.get(0));
        mockInputPortConnection(caseInsensitiveBlock, 1, inputDataMap.get(1));

        caseInsensitiveBlock.calculateOutput(0.0);

        double output = getScalarOutputForBlock(caseInsensitiveBlock, 0);
        assertEquals("Case-insensitive: 'Hello' == 'hello'", 1.0, output, DELTA);
    }

    @Test
    public void testEmptyStrings() {
        // Test: Empty strings comparison
        setStringInput(0, "");
        setStringInput(1, "");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Empty strings should be equal", 1.0, output, DELTA);
    }

    @Test
    public void testEmptyVsNonEmpty() {
        // Test: Empty string vs non-empty string
        setStringInput(0, "");
        setStringInput(1, "Test");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Empty != non-empty", 0.0, output, DELTA);
    }

    @Test
    public void testStringsWithSpaces() {
        // Test: Strings with different spacing
        setStringInput(0, "Hello World");
        setStringInput(1, "HelloWorld");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Spaces should matter in comparison", 0.0, output, DELTA);
    }

    @Test
    public void testLeadingTrailingSpaces() {
        // Test: Leading/trailing spaces should matter
        setStringInput(0, "Hello");
        setStringInput(1, " Hello ");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Leading/trailing spaces should matter", 0.0, output, DELTA);
    }

    @Test
    public void testSpecialCharacters() {
        // Test: Special characters comparison
        setStringInput(0, "Line1\nLine2");
        setStringInput(1, "Line1\nLine2");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Special characters should be compared", 1.0, output, DELTA);
    }

    @Test
    public void testUnicodeStrings() {
        // Test: Unicode character comparison
        setStringInput(0, "Hello 世界");
        setStringInput(1, "Hello 世界");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Unicode strings should be compared", 1.0, output, DELTA);
    }

    @Test
    public void testNumericStrings() {
        // Test: Numeric strings as text
        setStringInput(0, "123.45");
        setStringInput(1, "123.45");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Numeric strings should be compared as text", 1.0, output, DELTA);
    }

    @Test
    public void testSubstringNotEqual() {
        // Test: Substring should not equal full string
        setStringInput(0, "Hello");
        setStringInput(1, "Hello World");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Substring should not equal full string", 0.0, output, DELTA);
    }

    @Test
    public void testMultipleComparisons() {
        // Test: Multiple comparisons in sequence
        String[][] testCases = {
            {"Test", "Test", "1.0"},
            {"Test", "test", "0.0"},
            {"ABC", "ABC", "1.0"},
            {"123", "456", "0.0"},
            {"", "", "1.0"}
        };

        for (String[] testCase : testCases) {
            setStringInput(0, testCase[0]);
            setStringInput(1, testCase[1]);

            block.calculateOutput(0.0);

            double expectedOutput = Double.parseDouble(testCase[2]);
            double actualOutput = getScalarOutput(0);
            assertEquals(String.format("Comparing '%s' with '%s'", testCase[0], testCase[1]),
                expectedOutput, actualOutput, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setStringInput(0, "Test String");
        setStringInput(1, "Test String");

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average StringCompare calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 20 microseconds on average
        assertTrue("StringCompare calculateOutput should be fast", avgTime < 20000);
    }
}
