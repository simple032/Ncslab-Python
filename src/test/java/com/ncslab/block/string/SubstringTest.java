package com.ncslab.block.string;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ncslab.block.Block;
import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.StringData;
import com.ncslab.block.io.OutputPort;

/**
 * Comprehensive test suite for Substring block.
 *
 * Tests cover:
 * - Basic substring extraction with various start/length
 * - 1-based MATLAB indexing convention
 * - Edge cases (out of bounds, empty strings)
 * - Special characters
 * - Block configuration (ports, parameters)
 * - DTO-based factory methods
 *
 * @author NCSLab Team
 * @version 2025
 */
public class SubstringTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create substring extractor: start at index 1 (1st char), length 5
        return Substring.create("testSubstring", "test", 1, 5, mockModel);
    }

    // ============================================================================
    // BASIC SUBSTRING EXTRACTION TESTS
    // ============================================================================

    @Test
    public void testBasicSubstringExtraction() {
        setStringInput(0, "HelloWorld");
        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Hello", output); // Start=1, Length=5 extracts "Hello"
    }

    @Test
    public void testExtractFromMiddle() {
        Substring subBlock = Substring.create("test", "test", 6, 5, mockModel);
        initializeOutputSignals(subBlock);
        subBlock.calculateInit();

        setStringInputForBlock(subBlock, 0, "HelloWorld");
        subBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(subBlock, 0);
        assertEquals("World", output); // Start=6, Length=5 extracts "World"
    }

    @Test
    public void testExtractSingleCharacter() {
        Substring subBlock = Substring.create("test", "test", 1, 1, mockModel);
        initializeOutputSignals(subBlock);
        subBlock.calculateInit();

        setStringInputForBlock(subBlock, 0, "Test");
        subBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(subBlock, 0);
        assertEquals("T", output); // Start=1, Length=1 extracts first char
    }

    @Test
    public void testExtractLastCharacter() {
        Substring subBlock = Substring.create("test", "test", 5, 1, mockModel);
        initializeOutputSignals(subBlock);
        subBlock.calculateInit();

        setStringInputForBlock(subBlock, 0, "Hello");
        subBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(subBlock, 0);
        assertEquals("o", output); // Start=5, Length=1 extracts last char
    }

    @Test
    public void testExtractEntireString() {
        Substring subBlock = Substring.create("test", "test", 1, 10, mockModel);
        initializeOutputSignals(subBlock);
        subBlock.calculateInit();

        setStringInputForBlock(subBlock, 0, "Test");
        subBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(subBlock, 0);
        assertEquals("Test", output); // Length exceeds string, extract to end
    }

    // ============================================================================
    // EDGE CASE TESTS
    // ============================================================================

    @Test
    public void testStartIndexBeyondStringLength() {
        Substring subBlock = Substring.create("test", "test", 20, 5, mockModel);
        initializeOutputSignals(subBlock);
        subBlock.calculateInit();

        setStringInputForBlock(subBlock, 0, "Short");
        subBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(subBlock, 0);
        assertEquals("", output); // Start beyond string returns empty
    }

    @Test
    public void testLengthExceedsRemainingString() {
        Substring subBlock = Substring.create("test", "test", 8, 10, mockModel);
        initializeOutputSignals(subBlock);
        subBlock.calculateInit();

        setStringInputForBlock(subBlock, 0, "HelloWorld");
        subBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(subBlock, 0);
        assertEquals("rld", output); // Start=8, extract "rld" (only 3 chars remain)
    }

    @Test
    public void testEmptyInputString() {
        setStringInput(0, "");
        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("", output); // Empty input returns empty
    }

    @Test
    public void testExtractFromSingleCharString() {
        Substring subBlock = Substring.create("test", "test", 1, 1, mockModel);
        initializeOutputSignals(subBlock);
        subBlock.calculateInit();

        setStringInputForBlock(subBlock, 0, "A");
        subBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(subBlock, 0);
        assertEquals("A", output);
    }

    // ============================================================================
    // 1-BASED MATLAB INDEXING TESTS
    // ============================================================================

    @Test
    public void testMatlabOneBasedIndexing() {
        Substring subBlock = Substring.create("test", "test", 1, 3, mockModel);
        initializeOutputSignals(subBlock);
        subBlock.calculateInit();

        setStringInputForBlock(subBlock, 0, "ABCDEF");
        subBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(subBlock, 0);
        assertEquals("ABC", output); // MATLAB index 1 = first character
    }

    @Test
    public void testIndexTwoExtractsFromSecondChar() {
        Substring subBlock = Substring.create("test", "test", 2, 3, mockModel);
        initializeOutputSignals(subBlock);
        subBlock.calculateInit();

        setStringInputForBlock(subBlock, 0, "ABCDEF");
        subBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(subBlock, 0);
        assertEquals("BCD", output); // MATLAB index 2 = second character
    }

    // ============================================================================
    // SPECIAL CHARACTER TESTS
    // ============================================================================

    @Test
    public void testSubstringWithSpaces() {
        Substring subBlock = Substring.create("test", "test", 1, 5, mockModel);
        initializeOutputSignals(subBlock);
        subBlock.calculateInit();

        setStringInputForBlock(subBlock, 0, "Hello World");
        subBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(subBlock, 0);
        assertEquals("Hello", output);
    }

    @Test
    public void testSubstringWithNumbers() {
        Substring subBlock = Substring.create("test", "test", 5, 3, mockModel);
        initializeOutputSignals(subBlock);
        subBlock.calculateInit();

        setStringInputForBlock(subBlock, 0, "Test123456");
        subBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(subBlock, 0);
        assertEquals("123", output);
    }

    @Test
    public void testSubstringWithSpecialChars() {
        Substring subBlock = Substring.create("test", "test", 6, 5, mockModel);
        initializeOutputSignals(subBlock);
        subBlock.calculateInit();

        setStringInputForBlock(subBlock, 0, "Hello!@#$%World");
        subBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(subBlock, 0);
        assertEquals("!@#$%", output);
    }

    // ============================================================================
    // BLOCK CONFIGURATION TESTS
    // ============================================================================

    @Test
    public void testBlockHasOneInputPort() {
        assertEquals("Substring should have 1 input port", 1, block.getInputPortList().size());
    }

    @Test
    public void testBlockHasOneOutputPort() {
        assertEquals("Substring should have 1 output port", 1, block.getOutputPortList().size());
    }

    @Test
    public void testBlockType() {
        assertEquals("Substring", block.getBlockType());
    }

    @Test
    public void testOutputPortDimensions() {
        OutputPort outputPort = block.getOutputPortList().get(0);
        assertEquals("String output should be 1x1", 1, outputPort.getHeight());
        assertEquals("String output should be 1x1", 1, outputPort.getWidth());
    }

    // ============================================================================
    // PARAMETER ACCESS TESTS
    // ============================================================================

    @Test
    public void testGetStartIndexValue() {
        Substring subBlock = (Substring) block;
        assertEquals(1, subBlock.getStartIndexValue());
    }

    @Test
    public void testGetLengthValue() {
        Substring subBlock = (Substring) block;
        assertEquals(5, subBlock.getLengthValue());
    }

    // ============================================================================
    // FACTORY METHOD TESTS
    // ============================================================================

    @Test
    public void testCreateWithSimpleFactory() {
        Substring subBlock = Substring.create("simple", "test", mockModel);
        assertNotNull(subBlock);
        assertEquals("simple", subBlock.getBlockName());
        assertEquals(1, subBlock.getStartIndexValue()); // Default start=1
        assertEquals(1, subBlock.getLengthValue()); // Default length=1
    }

    @Test
    public void testCreateWithFullFactory() {
        Substring subBlock = Substring.create("full", "test", 3, 7, mockModel);
        assertNotNull(subBlock);
        assertEquals("full", subBlock.getBlockName());
        assertEquals(3, subBlock.getStartIndexValue());
        assertEquals(7, subBlock.getLengthValue());
    }

    // ============================================================================
    // HELPER METHODS FOR TESTING ADDITIONAL BLOCKS
    // ============================================================================

    /**
     * Set string input for a specific block's input port
     */
    private void setStringInputForBlock(Block targetBlock, int inputPortIndex, String value) {
        com.ncslab.block.data.StringData inputData = new com.ncslab.block.data.StringData(value);
        mockInputPortConnection(targetBlock, inputPortIndex, inputData);
    }

    /**
     * Get string output from a specific block's output port
     */
    private String getStringOutputForBlock(Block targetBlock, int outputPortIndex) {
        Data outputData = getOutputDataForBlock(targetBlock, outputPortIndex);

        if (outputData instanceof StringData) {
            return ((StringData) outputData).getStringValue();
        }

        // Fallback
        String str = outputData.getInitString();
        if (str != null) {
            return str;
        }

        return outputData.getDataString();
    }
}
