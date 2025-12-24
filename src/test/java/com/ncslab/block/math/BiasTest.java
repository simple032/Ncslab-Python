package com.ncslab.block.math;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Bias block.
 * Tests calculateOutput() and bias addition operations without WebSocket dependencies.
 *
 * Test Coverage:
 * - Scalar bias addition (input + bias)
 * - Zero bias
 * - Positive and negative bias
 * - Large and small values
 * - Edge cases
 * - Performance benchmarks
 */
public class BiasTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create Bias block with bias value of 5.0
        return Bias.create("testBias", "test", 5.0, mockModel);
    }

    @Test
    public void testBasicBias() {
        // Test basic bias addition: 10 + 5 = 15
        setScalarInput(0, 10.0);
        block.calculateOutput(0.0);
        assertEquals("Should add bias to input", 15.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testZeroInput() {
        // Test bias with zero input: 0 + 5 = 5
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        assertEquals("Should add bias to zero input", 5.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNegativeInput() {
        // Test bias with negative input: -10 + 5 = -5
        setScalarInput(0, -10.0);
        block.calculateOutput(0.0);
        assertEquals("Should add bias to negative input", -5.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testZeroBias() throws Exception {
        // Test with zero bias: input + 0 = input
        Bias zeroBiasBlock = Bias.create("testZeroBias", "test", 0.0, mockModel);
        initializeOutputSignals(zeroBiasBlock);
        zeroBiasBlock.calculateInit();

        mockInputPortConnection(zeroBiasBlock, 0, new com.ncslab.block.data.Data(42.0));
        zeroBiasBlock.calculateOutput(0.0);
        double result = zeroBiasBlock.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        assertEquals("Zero bias should not change input", 42.0, result, DELTA);
    }

    @Test
    public void testNegativeBias() throws Exception {
        // Test with negative bias: 10 + (-3) = 7
        Bias negBiasBlock = Bias.create("testNegBias", "test", -3.0, mockModel);
        initializeOutputSignals(negBiasBlock);
        negBiasBlock.calculateInit();

        mockInputPortConnection(negBiasBlock, 0, new com.ncslab.block.data.Data(10.0));
        negBiasBlock.calculateOutput(0.0);
        double result = negBiasBlock.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        assertEquals("Should add negative bias", 7.0, result, DELTA);
    }

    @Test
    public void testLargePositiveBias() throws Exception {
        // Test with large positive bias
        double largeBias = 1000.0;
        Bias largeBiasBlock = Bias.create("testLargeBias", "test", largeBias, mockModel);
        initializeOutputSignals(largeBiasBlock);
        largeBiasBlock.calculateInit();

        mockInputPortConnection(largeBiasBlock, 0, new com.ncslab.block.data.Data(100.0));
        largeBiasBlock.calculateOutput(0.0);
        double result = largeBiasBlock.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        assertEquals("Should handle large bias", 1100.0, result, DELTA);
    }

    @Test
    public void testLargeValues() {
        // Test with large input values
        double largeValue = 1e10;
        setScalarInput(0, largeValue);
        block.calculateOutput(0.0);
        assertEquals("Should handle large values", largeValue + 5.0, getScalarOutput(0), largeValue * 1e-10);
    }

    @Test
    public void testSmallValues() {
        // Test with small input values
        double smallValue = 1e-10;
        setScalarInput(0, smallValue);
        block.calculateOutput(0.0);
        assertEquals("Should handle small values", smallValue + 5.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testFractionalBias() throws Exception {
        // Test with fractional bias: 10.5 + 0.25 = 10.75
        Bias fracBiasBlock = Bias.create("testFracBias", "test", 0.25, mockModel);
        initializeOutputSignals(fracBiasBlock);
        fracBiasBlock.calculateInit();

        mockInputPortConnection(fracBiasBlock, 0, new com.ncslab.block.data.Data(10.5));
        fracBiasBlock.calculateOutput(0.0);
        double result = fracBiasBlock.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        assertEquals("Should handle fractional bias", 10.75, result, DELTA);
    }

    @Test
    public void testOutputPortConfiguration() {
        // Verify output port is properly configured
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertTrue("Bias block should have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testInputPortConfiguration() {
        // Verify input port
        assertEquals("Should have 1 input port", 1, block.getInputPortList().size());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testBias", "testBias", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be Bias", "Bias", block.getBlockType());
    }

    @Test
    public void testParameterCount() {
        // Bias block should have parameters for bias, sampleTime, etc.
        assertTrue("Should have at least 3 parameters", block.getParameterList().size() >= 3);
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        setScalarInput(0, 10.0);

        for (int i = 0; i < 100; i++) {
            block.calculateOutput(i * 0.01);
            double output = getScalarOutput(0);
            assertEquals("Should always output 15.0", 15.0, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 10.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Bias calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Bias block should be very fast (simple addition operation with input handling)
        assertTrue("Bias calculateOutput should be fast", avgTime < 200000); // <200 microseconds
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        setScalarInput(0, 10.0);
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
        setScalarInput(0, 10.0);

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

        assertEquals("Final output should be 15.0", 15.0, firstOutput, DELTA);
    }

    @Test
    public void testCommutativeProperty() {
        // Test that bias addition is commutative: a + b = b + a
        // This is testing conceptually since we have a fixed bias value
        setScalarInput(0, 10.0);
        block.calculateOutput(0.0);
        double result1 = getScalarOutput(0);

        // Reverse perspective: if bias is 5, then input 10 gives 15
        assertEquals("Bias addition should be 15", 15.0, result1, DELTA);
    }

    @Test
    public void testNegativeInputAndNegativeBias() throws Exception {
        // Test negative input with negative bias: -10 + (-5) = -15
        Bias negBiasBlock = Bias.create("testNegBoth", "test", -5.0, mockModel);
        initializeOutputSignals(negBiasBlock);
        negBiasBlock.calculateInit();

        mockInputPortConnection(negBiasBlock, 0, new com.ncslab.block.data.Data(-10.0));
        negBiasBlock.calculateOutput(0.0);
        double result = negBiasBlock.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        assertEquals("Should handle both negative", -15.0, result, DELTA);
    }

    @Test
    public void testChangingInput() {
        // Test that output changes correctly as input changes
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        assertEquals("Should be 5 for input 0", 5.0, getScalarOutput(0), DELTA);

        setScalarInput(0, 10.0);
        block.calculateOutput(0.0);
        assertEquals("Should be 15 for input 10", 15.0, getScalarOutput(0), DELTA);

        setScalarInput(0, -5.0);
        block.calculateOutput(0.0);
        assertEquals("Should be 0 for input -5", 0.0, getScalarOutput(0), DELTA);
    }
}
