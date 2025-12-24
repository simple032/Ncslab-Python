package com.ncslab.block.math;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for MinMax block.
 * Tests calculateOutput() and min/max operations without WebSocket dependencies.
 *
 * Test Coverage:
 * - min operation with 2+ inputs
 * - max operation with 2+ inputs
 * - Positive and negative values
 * - Zero handling and edge cases
 * - Multiple input configurations (2, 3, 4 inputs)
 * - Mathematical correctness validation
 * - Performance benchmarks
 */
public class MinMaxTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create default MinMax block (min, 2 inputs)
        return MinMax.create("testMinMax", "test", "min", 2, mockModel);
    }

    @Test
    public void testMinTwoInputs() {
        // min(5, 3) = 3
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);
        block.calculateOutput(0.0);
        assertEquals("min(5, 3) should be 3", 3.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testMinEqualInputs() {
        // min(5, 5) = 5
        setScalarInput(0, 5.0);
        setScalarInput(1, 5.0);
        block.calculateOutput(0.0);
        assertEquals("min(5, 5) should be 5", 5.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testMinNegativeValues() {
        // min(-3, -7) = -7
        setScalarInput(0, -3.0);
        setScalarInput(1, -7.0);
        block.calculateOutput(0.0);
        assertEquals("min(-3, -7) should be -7", -7.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testMinMixedSigns() {
        // min(5, -3) = -3
        setScalarInput(0, 5.0);
        setScalarInput(1, -3.0);
        block.calculateOutput(0.0);
        assertEquals("min(5, -3) should be -3", -3.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testMinWithZero() {
        // min(0, 5) = 0
        setScalarInput(0, 0.0);
        setScalarInput(1, 5.0);
        block.calculateOutput(0.0);
        assertEquals("min(0, 5) should be 0", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testMaxFunction() throws Exception {
        Block maxBlock = MinMax.create("testMax", "test", "max", 2, mockModel);
        initializeOutputSignals(maxBlock);
        maxBlock.calculateInit();

        // max(5, 3) = 5
        setScalarInputForBlock(maxBlock, 0, 5.0);
        setScalarInputForBlock(maxBlock, 1, 3.0);
        maxBlock.calculateOutput(0.0);
        assertEquals("max(5, 3) should be 5", 5.0, getScalarOutputForBlock(maxBlock, 0), DELTA);
    }

    @Test
    public void testMaxEqualInputs() throws Exception {
        Block maxBlock = MinMax.create("testMax", "test", "max", 2, mockModel);
        initializeOutputSignals(maxBlock);
        maxBlock.calculateInit();

        // max(5, 5) = 5
        setScalarInputForBlock(maxBlock, 0, 5.0);
        setScalarInputForBlock(maxBlock, 1, 5.0);
        maxBlock.calculateOutput(0.0);
        assertEquals("max(5, 5) should be 5", 5.0, getScalarOutputForBlock(maxBlock, 0), DELTA);
    }

    @Test
    public void testMaxNegativeValues() throws Exception {
        Block maxBlock = MinMax.create("testMax", "test", "max", 2, mockModel);
        initializeOutputSignals(maxBlock);
        maxBlock.calculateInit();

        // max(-3, -7) = -3
        setScalarInputForBlock(maxBlock, 0, -3.0);
        setScalarInputForBlock(maxBlock, 1, -7.0);
        maxBlock.calculateOutput(0.0);
        assertEquals("max(-3, -7) should be -3", -3.0, getScalarOutputForBlock(maxBlock, 0), DELTA);
    }

    @Test
    public void testMaxMixedSigns() throws Exception {
        Block maxBlock = MinMax.create("testMax", "test", "max", 2, mockModel);
        initializeOutputSignals(maxBlock);
        maxBlock.calculateInit();

        // max(5, -3) = 5
        setScalarInputForBlock(maxBlock, 0, 5.0);
        setScalarInputForBlock(maxBlock, 1, -3.0);
        maxBlock.calculateOutput(0.0);
        assertEquals("max(5, -3) should be 5", 5.0, getScalarOutputForBlock(maxBlock, 0), DELTA);
    }

    @Test
    public void testMinThreeInputs() throws Exception {
        Block minBlock = MinMax.create("testMin3", "test", "min", 3, mockModel);
        initializeOutputSignals(minBlock);
        minBlock.calculateInit();

        // min(5, 3, 7) = 3
        setScalarInputForBlock(minBlock, 0, 5.0);
        setScalarInputForBlock(minBlock, 1, 3.0);
        setScalarInputForBlock(minBlock, 2, 7.0);
        minBlock.calculateOutput(0.0);
        assertEquals("min(5, 3, 7) should be 3", 3.0, getScalarOutputForBlock(minBlock, 0), DELTA);
    }

    @Test
    public void testMaxThreeInputs() throws Exception {
        Block maxBlock = MinMax.create("testMax3", "test", "max", 3, mockModel);
        initializeOutputSignals(maxBlock);
        maxBlock.calculateInit();

        // max(5, 3, 7) = 7
        setScalarInputForBlock(maxBlock, 0, 5.0);
        setScalarInputForBlock(maxBlock, 1, 3.0);
        setScalarInputForBlock(maxBlock, 2, 7.0);
        maxBlock.calculateOutput(0.0);
        assertEquals("max(5, 3, 7) should be 7", 7.0, getScalarOutputForBlock(maxBlock, 0), DELTA);
    }

    @Test
    public void testMinFourInputs() throws Exception {
        Block minBlock = MinMax.create("testMin4", "test", "min", 4, mockModel);
        initializeOutputSignals(minBlock);
        minBlock.calculateInit();

        // min(5, 3, 7, 1) = 1
        setScalarInputForBlock(minBlock, 0, 5.0);
        setScalarInputForBlock(minBlock, 1, 3.0);
        setScalarInputForBlock(minBlock, 2, 7.0);
        setScalarInputForBlock(minBlock, 3, 1.0);
        minBlock.calculateOutput(0.0);
        assertEquals("min(5, 3, 7, 1) should be 1", 1.0, getScalarOutputForBlock(minBlock, 0), DELTA);
    }

    @Test
    public void testMaxFourInputs() throws Exception {
        Block maxBlock = MinMax.create("testMax4", "test", "max", 4, mockModel);
        initializeOutputSignals(maxBlock);
        maxBlock.calculateInit();

        // max(5, 3, 7, 1) = 7
        setScalarInputForBlock(maxBlock, 0, 5.0);
        setScalarInputForBlock(maxBlock, 1, 3.0);
        setScalarInputForBlock(maxBlock, 2, 7.0);
        setScalarInputForBlock(maxBlock, 3, 1.0);
        maxBlock.calculateOutput(0.0);
        assertEquals("max(5, 3, 7, 1) should be 7", 7.0, getScalarOutputForBlock(maxBlock, 0), DELTA);
    }

    @Test
    public void testMinLargeValues() throws Exception {
        Block minBlock = MinMax.create("testMinLarge", "test", "min", 2, mockModel);
        initializeOutputSignals(minBlock);
        minBlock.calculateInit();

        // min(1e10, 2e10) = 1e10
        setScalarInputForBlock(minBlock, 0, 1e10);
        setScalarInputForBlock(minBlock, 1, 2e10);
        minBlock.calculateOutput(0.0);
        assertEquals("min(1e10, 2e10) should be 1e10", 1e10, getScalarOutputForBlock(minBlock, 0), 1e5);
    }

    @Test
    public void testMaxSmallValues() throws Exception {
        Block maxBlock = MinMax.create("testMaxSmall", "test", "max", 2, mockModel);
        initializeOutputSignals(maxBlock);
        maxBlock.calculateInit();

        // max(1e-10, 2e-10) = 2e-10
        setScalarInputForBlock(maxBlock, 0, 1e-10);
        setScalarInputForBlock(maxBlock, 1, 2e-10);
        maxBlock.calculateOutput(0.0);
        assertEquals("max(1e-10, 2e-10) should be 2e-10", 2e-10, getScalarOutputForBlock(maxBlock, 0), 1e-15);
    }

    @Test
    public void testInputPortConfiguration2() {
        assertEquals("Should have 2 input ports", 2, block.getInputPortList().size());
    }

    @Test
    public void testInputPortConfiguration3() throws Exception {
        Block minBlock = MinMax.create("testMin3", "test", "min", 3, mockModel);
        assertEquals("Should have 3 input ports", 3, minBlock.getInputPortList().size());
    }

    @Test
    public void testInputPortConfiguration4() throws Exception {
        Block minBlock = MinMax.create("testMin4", "test", "min", 4, mockModel);
        assertEquals("Should have 4 input ports", 4, minBlock.getInputPortList().size());
    }

    @Test
    public void testOutputPortConfiguration() {
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertTrue("MinMax block should have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testMinMax", "testMinMax", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be MinMax", "MinMax", block.getBlockType());
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);

        for (int i = 0; i < 100; i++) {
            block.calculateOutput(i * 0.01);
            double output = getScalarOutput(0);
            assertEquals("Should always output 3 for min(5, 3)", 3.0, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average MinMax calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // MinMax should be fast (simple min/max operation)
        assertTrue("MinMax calculateOutput should be fast", avgTime < 200000); // <200 microseconds
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);
        block.calculateOutput(0.0);
        double firstOutput = getScalarOutput(0);

        // Reinitialize
        block.calculateInit();
        block.calculateOutput(0.0);
        double secondOutput = getScalarOutput(0);

        assertEquals("Output should be same after reinitialization", firstOutput, secondOutput, DELTA);
    }

    @Test
    public void testNoDriftOverTime() {
        // Verify that output doesn't drift over many iterations with constant inputs
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);

        double firstOutput = 0;
        for (int i = 0; i < 1000; i++) {
            block.calculateOutput(i * 0.001);
            double currentOutput = getScalarOutput(0);

            if (i == 0) {
                firstOutput = currentOutput;
            } else {
                assertEquals("Output should not drift at iteration " + i,
                    firstOutput, currentOutput, DELTA);
            }
        }

        assertEquals("Final output should be 3", 3.0, firstOutput, DELTA);
    }

    @Test
    public void testMinMaxRelationship() throws Exception {
        // Test that min(a,b) + max(a,b) = a + b
        Block maxBlock = MinMax.create("testMax", "test", "max", 2, mockModel);
        initializeOutputSignals(maxBlock);
        maxBlock.calculateInit();

        double a = 7.5;
        double b = 3.2;

        setScalarInput(0, a);
        setScalarInput(1, b);
        block.calculateOutput(0.0);
        double minValue = getScalarOutput(0);

        setScalarInputForBlock(maxBlock, 0, a);
        setScalarInputForBlock(maxBlock, 1, b);
        maxBlock.calculateOutput(0.0);
        double maxValue = getScalarOutputForBlock(maxBlock, 0);

        assertEquals("min + max should equal a + b", a + b, minValue + maxValue, DELTA);
    }

    @Test
    public void testMinAllNegative() throws Exception {
        Block minBlock = MinMax.create("testMinNeg", "test", "min", 3, mockModel);
        initializeOutputSignals(minBlock);
        minBlock.calculateInit();

        // min(-5, -3, -7) = -7
        setScalarInputForBlock(minBlock, 0, -5.0);
        setScalarInputForBlock(minBlock, 1, -3.0);
        setScalarInputForBlock(minBlock, 2, -7.0);
        minBlock.calculateOutput(0.0);
        assertEquals("min(-5, -3, -7) should be -7", -7.0, getScalarOutputForBlock(minBlock, 0), DELTA);
    }

    @Test
    public void testMaxAllNegative() throws Exception {
        Block maxBlock = MinMax.create("testMaxNeg", "test", "max", 3, mockModel);
        initializeOutputSignals(maxBlock);
        maxBlock.calculateInit();

        // max(-5, -3, -7) = -3
        setScalarInputForBlock(maxBlock, 0, -5.0);
        setScalarInputForBlock(maxBlock, 1, -3.0);
        setScalarInputForBlock(maxBlock, 2, -7.0);
        maxBlock.calculateOutput(0.0);
        assertEquals("max(-5, -3, -7) should be -3", -3.0, getScalarOutputForBlock(maxBlock, 0), DELTA);
    }
}
