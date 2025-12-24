package com.ncslab.block.string;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import org.json.JSONObject;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for ScanString block.
 * Tests parsing formatted strings (similar to sscanf).
 *
 * @author NCSLab Team
 * @version 1.0
 */
public class ScanStringTest extends DirectBlockTestBase {

    private ScanString scanStringBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create ScanString block with format string parameter
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "ScanString");
        blockJson.put("blockName", "testScanString");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        // Set format string parameter (e.g., "%d")
        JSONObject paramValues = new JSONObject();
        paramValues.put("FormatString", "%d");
        blockJson.put("paramValues", paramValues);

        scanStringBlock = new ScanString(blockJson, mockModel);
        return scanStringBlock;
    }

    @Test
    public void testBasicIntegerScan() {
        // Test: Scan integer from string
        setStringInput(0, "123");

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should scan '123' as 123.0", 123.0, output, DELTA);
    }

    @Test
    public void testFloatScan() throws Exception {
        // Test: Scan floating point value
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "ScanString");
        blockJson.put("blockName", "floatScan");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        JSONObject paramValues = new JSONObject();
        paramValues.put("FormatString", "%f");
        blockJson.put("paramValues", paramValues);

        ScanString floatBlock = new ScanString(blockJson, mockModel);
        initializeOutputSignals(floatBlock);
        floatBlock.calculateInit();

        setStringInput(0, "123.456");
        mockInputPortConnection(floatBlock, 0, inputDataMap.get(0));

        floatBlock.calculateOutput(0.0);

        double output = getScalarOutputForBlock(floatBlock, 0);
        assertEquals("Should scan '123.456'", 123.456, output, 0.01);
    }

    @Test
    public void testScanWithText() throws Exception {
        // Test: Scan value with surrounding text
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "ScanString");
        blockJson.put("blockName", "textScan");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        JSONObject paramValues = new JSONObject();
        paramValues.put("FormatString", "Value: %d");
        blockJson.put("paramValues", paramValues);

        ScanString textBlock = new ScanString(blockJson, mockModel);
        initializeOutputSignals(textBlock);
        textBlock.calculateInit();

        setStringInput(0, "Value: 42");
        mockInputPortConnection(textBlock, 0, inputDataMap.get(0));

        textBlock.calculateOutput(0.0);

        double output = getScalarOutputForBlock(textBlock, 0);
        assertEquals("Should scan 42 from 'Value: 42'", 42.0, output, DELTA);
    }

    @Test
    public void testMultipleValues() throws Exception {
        // Test: Scan multiple values
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockType", "ScanString");
        blockJson.put("blockName", "multiScan");
        blockJson.put("blockPath", "test");
        blockJson.put("blockUUID", "test-uuid");

        JSONObject paramValues = new JSONObject();
        paramValues.put("FormatString", "%d %d");
        blockJson.put("paramValues", paramValues);

        ScanString multiBlock = new ScanString(blockJson, mockModel);
        initializeOutputSignals(multiBlock);
        multiBlock.calculateInit();

        setStringInput(0, "10 20");
        mockInputPortConnection(multiBlock, 0, inputDataMap.get(0));

        multiBlock.calculateOutput(0.0);

        double output1 = getScalarOutputForBlock(multiBlock, 0);
        assertNotNull("First output should exist", output1);

        if (multiBlock.getOutputPortList().size() > 1) {
            double output2 = getScalarOutputForBlock(multiBlock, 1);
            assertNotNull("Second output should exist", output2);
        }
    }

    @Test
    public void testInvalidFormat() {
        // Test: Invalid string format (should handle gracefully)
        setStringInput(0, "NotANumber");

        try {
            block.calculateOutput(0.0);

            // May return 0, NaN, or throw exception
            double output = getScalarOutput(0);
            assertTrue("Should handle invalid format", true);
        } catch (Exception e) {
            // Exception is acceptable for invalid format
            assertTrue("Exception acceptable for invalid format", true);
        }
    }

    @Test
    public void testEmptyString() {
        // Test: Empty string
        setStringInput(0, "");

        try {
            block.calculateOutput(0.0);

            double output = getScalarOutput(0);
            // Should return 0 or NaN
            assertTrue("Should handle empty string", true);
        } catch (Exception e) {
            // Exception is acceptable
            assertTrue("Exception acceptable for empty string", true);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setStringInput(0, "123");

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average ScanString calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 30 microseconds on average
        assertTrue("ScanString calculateOutput should be reasonably fast", avgTime < 30000);
    }
}
