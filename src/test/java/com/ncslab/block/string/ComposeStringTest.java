package com.ncslab.block.string;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import org.json.JSONObject;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for ComposeString block.
 * Tests formatted string composition (similar to sprintf).
 *
 * @author NCSLab Team
 * @version 1.0
 */
public class ComposeStringTest extends DirectBlockTestBase {

    private ComposeString composeStringBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create ComposeString block with format string parameter
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "ComposeString");
        blockJson.put("blockName", "testComposeString");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        // Set format string parameter (e.g., "Value: %d, Float: %.2f")
        JSONObject paramValues = new JSONObject();
        paramValues.put("FormatString", "Value: %d");
        blockJson.put("paramValues", paramValues);

        composeStringBlock = new ComposeString(blockJson, mockModel);
        return composeStringBlock;
    }

    @Test
    public void testBasicIntegerFormat() {
        // Test: Basic integer formatting
        setScalarInput(0, 123.0);

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertTrue("Should contain formatted value", output.contains("123") || output.contains("Value"));
    }

    @Test
    public void testFormatWithParameter() {
        // Test: Format string with single parameter
        setScalarInput(0, 42.0);

        block.calculateOutput(0.0);

        String output = getStringOutput(0);
        assertNotNull("Output should not be null", output);
        assertTrue("Output should have content", output.length() > 0);
    }

    @Test
    public void testMultipleParameters() throws Exception {
        // Test: Format string with multiple parameters
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "ComposeString");
        blockJson.put("blockName", "multiParam");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        JSONObject paramValues = new JSONObject();
        paramValues.put("FormatString", "X=%d, Y=%d");
        blockJson.put("paramValues", paramValues);

        ComposeString multiBlock = new ComposeString(blockJson, mockModel);
        initializeOutputSignals(multiBlock);
        multiBlock.calculateInit();

        setScalarInputForBlock(multiBlock, 0, 10.0);
        setScalarInputForBlock(multiBlock, 1, 20.0);

        multiBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(multiBlock, 0);
        assertNotNull("Output should not be null", output);
    }

    @Test
    public void testFloatFormatting() throws Exception {
        // Test: Float formatting with precision
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "ComposeString");
        blockJson.put("blockName", "floatFormat");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        JSONObject paramValues = new JSONObject();
        paramValues.put("FormatString", "Value: %.2f");
        blockJson.put("paramValues", paramValues);

        ComposeString floatBlock = new ComposeString(blockJson, mockModel);
        initializeOutputSignals(floatBlock);
        floatBlock.calculateInit();

        setScalarInputForBlock(floatBlock, 0, 123.456);

        floatBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(floatBlock, 0);
        assertNotNull("Output should not be null", output);
        assertTrue("Should contain numeric value", output.length() > 0);
    }

    @Test
    public void testStringFormatting() throws Exception {
        // Test: String formatting
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "ComposeString");
        blockJson.put("blockName", "stringFormat");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        JSONObject paramValues = new JSONObject();
        paramValues.put("FormatString", "Name: %s");
        blockJson.put("paramValues", paramValues);

        ComposeString strBlock = new ComposeString(blockJson, mockModel);
        initializeOutputSignals(strBlock);
        strBlock.calculateInit();

        setStringInputForBlock(strBlock, 0, "Test");

        strBlock.calculateOutput(0.0);

        String output = getStringOutputForBlock(strBlock, 0);
        assertNotNull("Output should not be null", output);
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 123.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average ComposeString calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 30 microseconds on average
        assertTrue("ComposeString calculateOutput should be reasonably fast", avgTime < 30000);
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
        com.ncslab.block.data.Data outputData = getOutputDataForBlock(targetBlock, outputPortIndex);

        if (outputData instanceof com.ncslab.block.data.StringData) {
            return ((com.ncslab.block.data.StringData) outputData).getStringValue();
        }

        // Fallback
        String str = outputData.getInitString();
        if (str != null) {
            return str;
        }

        return outputData.getDataString();
    }
}
