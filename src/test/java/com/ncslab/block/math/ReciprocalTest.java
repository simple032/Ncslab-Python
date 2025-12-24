package com.ncslab.block.math;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Reciprocal block.
 * Tests calculateOutput() and reciprocal (1/x) operations without WebSocket dependencies.
 *
 * Test Coverage:
 * - Scalar reciprocal (1/x)
 * - Division by zero handling (infinity)
 * - Saturation handling
 * - Zero values and negative values
 * - Large and small values
 * - Edge cases (infinity, NaN handling)
 * - Performance benchmarks
 */
public class ReciprocalTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create Reciprocal block without saturation
        return Reciprocal.create("testReciprocal", "test", false, mockModel);
    }

    @Test
    public void testBasicReciprocal() {
        // Test basic reciprocal: 1/2 = 0.5
        setScalarInput(0, 2.0);
        block.calculateOutput(0.0);
        assertEquals("Should compute reciprocal of 2", 0.5, getScalarOutput(0), DELTA);
    }

    @Test
    public void testReciprocalOfOne() {
        // Test reciprocal of 1: 1/1 = 1
        setScalarInput(0, 1.0);
        block.calculateOutput(0.0);
        assertEquals("Reciprocal of 1 should be 1", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testReciprocalOfZero() {
        // Test reciprocal of zero: 1/0 = infinity
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        double result = getScalarOutput(0);
        assertTrue("Reciprocal of zero should be infinity", Double.isInfinite(result) && result > 0);
    }

    @Test
    public void testReciprocalOfNegative() {
        // Test reciprocal of negative: 1/(-2) = -0.5
        setScalarInput(0, -2.0);
        block.calculateOutput(0.0);
        assertEquals("Should compute reciprocal of -2", -0.5, getScalarOutput(0), DELTA);
    }

    @Test
    public void testReciprocalOfLargeValue() {
        // Test reciprocal of large value: 1/1000 = 0.001
        setScalarInput(0, 1000.0);
        block.calculateOutput(0.0);
        assertEquals("Should compute reciprocal of large value", 0.001, getScalarOutput(0), DELTA);
    }

    @Test
    public void testReciprocalOfSmallValue() {
        // Test reciprocal of small value: 1/0.001 = 1000
        setScalarInput(0, 0.001);
        block.calculateOutput(0.0);
        assertEquals("Should compute reciprocal of small value", 1000.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testReciprocalOfFraction() {
        // Test reciprocal of fraction: 1/0.25 = 4
        setScalarInput(0, 0.25);
        block.calculateOutput(0.0);
        assertEquals("Should compute reciprocal of 0.25", 4.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSaturationEnabled() throws Exception {
        // Test reciprocal with saturation enabled
        Reciprocal satBlock = Reciprocal.create("testSat", "test", true, 10.0, -10.0,
                                               -1.0, "Inherit: Same as input", false, mockModel);
        initializeOutputSignals(satBlock);
        satBlock.calculateInit();

        // Test value that would exceed upper limit: 1/0.01 = 100, saturated to 10
        mockInputPortConnection(satBlock, 0, new com.ncslab.block.data.Data(0.01));
        satBlock.calculateOutput(0.0);
        double result = satBlock.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        assertEquals("Should saturate at upper limit", 10.0, result, DELTA);
    }

    @Test
    public void testSaturationLowerLimit() throws Exception {
        // Test reciprocal with saturation at lower limit
        Reciprocal satBlock = Reciprocal.create("testSatLower", "test", true, 10.0, -10.0,
                                               -1.0, "Inherit: Same as input", false, mockModel);
        initializeOutputSignals(satBlock);
        satBlock.calculateInit();

        // Test value that would be below lower limit: 1/(-0.01) = -100, saturated to -10
        mockInputPortConnection(satBlock, 0, new com.ncslab.block.data.Data(-0.01));
        satBlock.calculateOutput(0.0);
        double result = satBlock.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        assertEquals("Should saturate at lower limit", -10.0, result, DELTA);
    }

    @Test
    public void testReciprocalOfNegativeOne() {
        // Test reciprocal of -1: 1/(-1) = -1
        setScalarInput(0, -1.0);
        block.calculateOutput(0.0);
        assertEquals("Reciprocal of -1 should be -1", -1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testOutputPortConfiguration() {
        // Verify output port is properly configured
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertTrue("Reciprocal block should have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testInputPortConfiguration() {
        // Verify input port
        assertEquals("Should have 1 input port", 1, block.getInputPortList().size());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testReciprocal", "testReciprocal", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be Reciprocal", "Reciprocal", block.getBlockType());
    }

    @Test
    public void testParameterCount() {
        // Reciprocal block should have parameters for enableSaturation, upperLimit, lowerLimit, etc.
        assertTrue("Should have at least 5 parameters", block.getParameterList().size() >= 5);
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        setScalarInput(0, 2.0);

        for (int i = 0; i < 100; i++) {
            block.calculateOutput(i * 0.01);
            double output = getScalarOutput(0);
            assertEquals("Should always output 0.5", 0.5, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 2.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Reciprocal calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Reciprocal block should be fast (simple division operation with input handling)
        assertTrue("Reciprocal calculateOutput should be fast", avgTime < 200000); // <200 microseconds
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        setScalarInput(0, 2.0);
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
        setScalarInput(0, 2.0);

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

        assertEquals("Final output should be 0.5", 0.5, firstOutput, DELTA);
    }

    @Test
    public void testDoubleReciprocal() {
        // Test double reciprocal: 1/(1/x) = x (not directly testable with single block)
        // Instead test that reciprocal of reciprocal value returns original
        // If x = 2, then 1/x = 0.5, then 1/0.5 = 2
        setScalarInput(0, 0.5);
        block.calculateOutput(0.0);
        assertEquals("Reciprocal of 0.5 should be 2", 2.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testVeryLargeInput() {
        // Test reciprocal of very large value approaches zero
        double largeValue = 1e10;
        setScalarInput(0, largeValue);
        block.calculateOutput(0.0);
        assertEquals("Reciprocal of very large value should be very small",
                    1.0 / largeValue, getScalarOutput(0), 1e-20);
    }

    @Test
    public void testVerySmallInput() {
        // Test reciprocal of very small value is very large
        double smallValue = 1e-5;
        setScalarInput(0, smallValue);
        block.calculateOutput(0.0);
        assertEquals("Reciprocal of very small value should be large",
                    1.0 / smallValue, getScalarOutput(0), (1.0 / smallValue) * 1e-10);
    }
}
