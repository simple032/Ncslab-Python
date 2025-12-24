package com.ncslab.block.string;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ncslab.block.Block;
import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.StringData;
import com.ncslab.block.io.OutputPort;

/**
 * Comprehensive test suite for StringConcatenate block.
 *
 * Tests cover:
 * - Basic concatenation with 2 inputs
 * - Concatenation with 3+ inputs
 * - Empty string handling
 * - Maximum length constraints
 * - Special characters
 * - Block configuration (ports, parameters)
 * - DTO-based factory methods
 *
 * @author NCSLab Team
 * @version 2025
 */
public class StringConcatenateTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        return StringConcatenate.create("testStringConcat", "test", 2, mockModel);
    }

    // ============================================================================
    // BASIC CONCATENATION TESTS (2 inputs)
    // ============================================================================

    @Test
    public void testTwoStringConcatenation() {
        setStringInput(0, "Hello");
        setStringInput(1, "World");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("HelloWorld", output);
    }

    @Test
    public void testConcatenationWithSpace() {
        setStringInput(0, "Hello");
        setStringInput(1, " World");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Hello World", output);
    }

    @Test
    public void testEmptyFirstString() {
        setStringInput(0, "");
        setStringInput(1, "World");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("World", output);
    }

    @Test
    public void testEmptySecondString() {
        setStringInput(0, "Hello");
        setStringInput(1, "");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Hello", output);
    }

    @Test
    public void testBothStringsEmpty() {
        setStringInput(0, "");
        setStringInput(1, "");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("", output);
    }

    // ============================================================================
    // MULTIPLE INPUT TESTS (3+ inputs)
    // ============================================================================

    @Test
    public void testThreeInputConcatenation() {
        StringConcatenate concatBlock = StringConcatenate.create("test", "test", 3, mockModel);
        initializeOutputSignals(concatBlock);
        concatBlock.calculateInit();

        setStringInputForBlock(concatBlock, 0, "Hello");
        setStringInputForBlock(concatBlock, 1, " ");
        setStringInputForBlock(concatBlock, 2, "World");

        concatBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(concatBlock, 0);
        assertEquals("Hello World", output);
    }

    @Test
    public void testFourInputConcatenation() {
        StringConcatenate concatBlock = StringConcatenate.create("test", "test", 4, mockModel);
        initializeOutputSignals(concatBlock);
        concatBlock.calculateInit();

        setStringInputForBlock(concatBlock, 0, "A");
        setStringInputForBlock(concatBlock, 1, "B");
        setStringInputForBlock(concatBlock, 2, "C");
        setStringInputForBlock(concatBlock, 3, "D");

        concatBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(concatBlock, 0);
        assertEquals("ABCD", output);
    }

    @Test
    public void testSingleInputConcatenation() {
        StringConcatenate concatBlock = StringConcatenate.create("test", "test", 1, mockModel);
        initializeOutputSignals(concatBlock);
        concatBlock.calculateInit();

        setStringInputForBlock(concatBlock, 0, "Single");

        concatBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(concatBlock, 0);
        assertEquals("Single", output);
    }

    // ============================================================================
    // MAXIMUM LENGTH CONSTRAINT TESTS
    // ============================================================================

    @Test
    public void testMaximumLengthTruncation() {
        StringConcatenate concatBlock = StringConcatenate.create("test", "test", 2, 5, "Fixed-size", mockModel);
        initializeOutputSignals(concatBlock);
        concatBlock.calculateInit();

        setStringInputForBlock(concatBlock, 0, "Hello");
        setStringInputForBlock(concatBlock, 1, "World");

        concatBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(concatBlock, 0);
        assertEquals("Result should be truncated to 5 chars", "Hello", output);
        assertEquals(5, output.length());
    }

    @Test
    public void testNoMaximumLength() {
        StringConcatenate concatBlock = StringConcatenate.create("test", "test", 2, 0, "Fixed-size", mockModel);
        initializeOutputSignals(concatBlock);
        concatBlock.calculateInit();

        setStringInputForBlock(concatBlock, 0, "Hello");
        setStringInputForBlock(concatBlock, 1, "World");

        concatBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(concatBlock, 0);
        assertEquals("HelloWorld", output);
    }

    @Test
    public void testMaxLengthLargerThanResult() {
        StringConcatenate concatBlock = StringConcatenate.create("test", "test", 2, 100, "Fixed-size", mockModel);
        initializeOutputSignals(concatBlock);
        concatBlock.calculateInit();

        setStringInputForBlock(concatBlock, 0, "Short");
        setStringInputForBlock(concatBlock, 1, "Text");

        concatBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(concatBlock, 0);
        assertEquals("ShortText", output);
    }

    // ============================================================================
    // SPECIAL CHARACTER TESTS
    // ============================================================================

    @Test
    public void testConcatenateWithNumbers() {
        setStringInput(0, "Test");
        setStringInput(1, "123");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Test123", output);
    }

    @Test
    public void testConcatenateWithSpecialChars() {
        setStringInput(0, "Hello!");
        setStringInput(1, "@World");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Hello!@World", output);
    }

    @Test
    public void testConcatenateWithNewlines() {
        setStringInput(0, "Line1\n");
        setStringInput(1, "Line2");

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertEquals("Line1\nLine2", output);
    }

    // ============================================================================
    // BLOCK CONFIGURATION TESTS
    // ============================================================================

    @Test
    public void testBlockHasTwoInputPortsByDefault() {
        assertEquals("StringConcatenate should have 2 input ports by default", 2, block.getInputPortList().size());
    }

    @Test
    public void testBlockHasOneOutputPort() {
        assertEquals("StringConcatenate should have 1 output port", 1, block.getOutputPortList().size());
    }

    @Test
    public void testDynamicInputPortCount() {
        StringConcatenate concatBlock = StringConcatenate.create("test", "test", 5, mockModel);
        assertEquals("StringConcatenate should have 5 input ports", 5, concatBlock.getInputPortList().size());
    }

    @Test
    public void testBlockType() {
        assertEquals("StringConcatenate", block.getBlockType());
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
    public void testGetNumberOfInputsValue() {
        StringConcatenate concatBlock = (StringConcatenate) block;
        assertEquals(2, concatBlock.getNumberOfInputsValue());
    }

    @Test
    public void testGetMaximumLengthValue() {
        StringConcatenate concatBlock = (StringConcatenate) block;
        assertEquals(0, concatBlock.getMaximumLengthValue());
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
