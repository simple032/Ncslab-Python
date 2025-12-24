package com.ncslab.block.string;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ncslab.block.Block;
import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.StringData;
import com.ncslab.block.io.OutputPort;

/**
 * Comprehensive test suite for StringConstant block.
 *
 * Tests cover:
 * - Basic string output functionality
 * - Empty strings
 * - Special characters
 * - Long strings
 * - Block metadata (ports, parameters, type)
 * - Sample time configuration
 * - DTO-based factory methods
 *
 * @author NCSLab Team
 * @version 2025
 */
public class StringConstantTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        return StringConstant.create("testStringConst", "test", "Hello World", mockModel);
    }

    // ============================================================================
    // BASIC FUNCTIONALITY TESTS
    // ============================================================================

    @Test
    public void testBasicStringOutput() {
        block.calculateOutput(0.0);
        String output = getStringOutput(0);
        assertEquals("Hello World", output);
    }

    @Test
    public void testSimpleString() {
        StringConstant stringBlock = StringConstant.create("test", "test", "Test", mockModel);
        initializeOutputSignals(stringBlock);
        stringBlock.calculateInit();
        stringBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(stringBlock, 0);
        assertEquals("Test", output);
    }

    @Test
    public void testEmptyString() {
        StringConstant stringBlock = StringConstant.create("test", "test", "", mockModel);
        initializeOutputSignals(stringBlock);
        stringBlock.calculateInit();
        stringBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(stringBlock, 0);
        assertEquals("", output);
    }

    @Test
    public void testSingleCharacter() {
        StringConstant stringBlock = StringConstant.create("test", "test", "A", mockModel);
        initializeOutputSignals(stringBlock);
        stringBlock.calculateInit();
        stringBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(stringBlock, 0);
        assertEquals("A", output);
    }

    @Test
    public void testLongString() {
        String longString = "This is a very long string that contains many characters to test the string constant block with longer text content.";
        StringConstant stringBlock = StringConstant.create("test", "test", longString, mockModel);
        initializeOutputSignals(stringBlock);
        stringBlock.calculateInit();
        stringBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(stringBlock, 0);
        assertEquals(longString, output);
    }

    // ============================================================================
    // SPECIAL CHARACTER TESTS
    // ============================================================================

    @Test
    public void testStringWithSpaces() {
        StringConstant stringBlock = StringConstant.create("test", "test", "Hello World Test", mockModel);
        initializeOutputSignals(stringBlock);
        stringBlock.calculateInit();
        stringBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(stringBlock, 0);
        assertEquals("Hello World Test", output);
    }

    @Test
    public void testStringWithNumbers() {
        StringConstant stringBlock = StringConstant.create("test", "test", "Test123", mockModel);
        initializeOutputSignals(stringBlock);
        stringBlock.calculateInit();
        stringBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(stringBlock, 0);
        assertEquals("Test123", output);
    }

    @Test
    public void testStringWithSpecialCharacters() {
        StringConstant stringBlock = StringConstant.create("test", "test", "Hello!@#$%", mockModel);
        initializeOutputSignals(stringBlock);
        stringBlock.calculateInit();
        stringBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(stringBlock, 0);
        assertEquals("Hello!@#$%", output);
    }

    @Test
    public void testStringWithQuotes() {
        StringConstant stringBlock = StringConstant.create("test", "test", "Hello\"World\"", mockModel);
        initializeOutputSignals(stringBlock);
        stringBlock.calculateInit();
        stringBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(stringBlock, 0);
        assertEquals("Hello\"World\"", output);
    }

    @Test
    public void testStringWithNewlines() {
        StringConstant stringBlock = StringConstant.create("test", "test", "Line1\nLine2", mockModel);
        initializeOutputSignals(stringBlock);
        stringBlock.calculateInit();
        stringBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(stringBlock, 0);
        assertEquals("Line1\nLine2", output);
    }

    // ============================================================================
    // OUTPUT DATA TYPE TESTS
    // ============================================================================

    @Test
    public void testOutputDataIsStringData() {
        block.calculateOutput(0.0);
        Data outputData = getOutputData(0);
        assertTrue("Output data should be StringData instance", outputData instanceof StringData);
    }

    @Test
    public void testStringDataProperties() {
        block.calculateOutput(0.0);
        Data outputData = getOutputData(0);
        StringData stringData = (StringData) outputData;

        assertEquals("Hello World", stringData.getStringValue());
        assertEquals(11, stringData.getLength());
        assertFalse(stringData.isEmpty());
    }

    // ============================================================================
    // BLOCK CONFIGURATION TESTS
    // ============================================================================

    @Test
    public void testBlockHasOneOutputPort() {
        assertEquals("StringConstant should have 1 output port", 1, block.getOutputPortList().size());
    }

    @Test
    public void testBlockHasNoInputPorts() {
        assertEquals("StringConstant should have 0 input ports", 0, block.getInputPortList().size());
    }

    @Test
    public void testBlockType() {
        assertEquals("StringConstant", block.getBlockType());
    }

    @Test
    public void testBlockName() {
        assertEquals("testStringConst", block.getBlockName());
    }

    @Test
    public void testOutputPortDimensions() {
        OutputPort outputPort = block.getOutputPortList().get(0);
        assertEquals("String output should be 1x1", 1, outputPort.getHeight());
        assertEquals("String output should be 1x1", 1, outputPort.getWidth());
    }

    // ============================================================================
    // FACTORY METHOD TESTS
    // ============================================================================

    @Test
    public void testCreateWithSimpleFactory() {
        StringConstant stringBlock = StringConstant.create("simple", "test", "Simple", mockModel);
        assertNotNull(stringBlock);
        assertEquals("simple", stringBlock.getBlockName());
        assertEquals("Simple", stringBlock.getStringValue());
    }

    @Test
    public void testCreateWithFullFactory() {
        StringConstant stringBlock = StringConstant.create(
            "full", "test", "Full", -1.0, "Inherit: Same as parameter", false, mockModel
        );
        assertNotNull(stringBlock);
        assertEquals("full", stringBlock.getBlockName());
        assertEquals("Full", stringBlock.getStringValue());
    }

    // ============================================================================
    // HELPER METHOD FOR TESTING ADDITIONAL BLOCKS
    // ============================================================================

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
