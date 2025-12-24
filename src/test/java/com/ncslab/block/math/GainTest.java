package com.ncslab.block.math;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Gain block.
 * Tests calculateOutput() directly without WebSocket dependencies.
 */
public class GainTest extends DirectBlockTestBase {

    private Gain gainBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create Gain block with gain value of 2.0
        gainBlock = Gain.create("testGain", "test", 2.0, mockModel);
        return gainBlock;
    }

    @Test
    public void testScalarMultiplication() {
        // Test: Gain of 2.0 should multiply input by 2
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Gain should multiply 5.0 by 2.0", 10.0, output, DELTA);
    }

    @Test
    public void testZeroInput() {
        // Test: Zero input should give zero output
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Zero input should give zero output", 0.0, output, DELTA);
    }

    @Test
    public void testNegativeInput() {
        // Test: Negative input handling
        setScalarInput(0, -3.0);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Gain should multiply -3.0 by 2.0", -6.0, output, DELTA);
    }

    @Test
    public void testMatrixInput() {
        // Test: Matrix element-wise multiplication
        double[][] inputMatrix = {{1.0, 2.0}, {3.0, 4.0}};
        setMatrixInput(0, inputMatrix);
        block.calculateOutput(0.0);

        double[][] expectedMatrix = {{2.0, 4.0}, {6.0, 8.0}};
        assertMatrixOutput(0, expectedMatrix);
    }

    @Test
    public void testLargeValues() {
        // Test: Large values
        setScalarInput(0, 1e10);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Gain should handle large values", 2e10, output, 1e-4);
    }

    @Test
    public void testSmallValues() {
        // Test: Small values
        setScalarInput(0, 1e-10);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Gain should handle small values", 2e-10, output, 1e-20);
    }

    @Test
    public void testFractionalGain() throws Exception {
        // Create a new Gain block with fractional gain
        Gain fractionalGain = Gain.create("fractionalGain", "test", 0.5, mockModel);
        fractionalGain.calculateInit();

        // Set input
        mockInputPortConnection(fractionalGain, 0, new Data(10.0));

        // Calculate output
        fractionalGain.calculateOutput(0.0);

        // Verify output
        Data outputData = fractionalGain.getOutputPortList().get(0).getOutputSignalC().getData();
        assertEquals("Fractional gain should multiply correctly", 5.0, outputData.getInitValue(), DELTA);
    }

    @Test
    public void testNegativeGain() throws Exception {
        // Create a new Gain block with negative gain
        Gain negativeGain = Gain.create("negativeGain", "test", -2.0, mockModel);
        negativeGain.calculateInit();

        // Set input
        mockInputPortConnection(negativeGain, 0, new Data(5.0));

        // Calculate output
        negativeGain.calculateOutput(0.0);

        // Verify output
        Data outputData = negativeGain.getOutputPortList().get(0).getOutputSignalC().getData();
        assertEquals("Negative gain should invert and multiply", -10.0, outputData.getInitValue(), DELTA);
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 5.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Gain calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 10 microseconds on average
        assertTrue("Gain calculateOutput should be fast", avgTime < 10000);
    }

    @Test
    public void testMultipleCalculations() {
        // Test: Multiple calculations with different inputs
        double[][] testCases = {
            {1.0, 2.0},
            {-5.0, -10.0},
            {0.0, 0.0},
            {100.0, 200.0},
            {-0.5, -1.0}
        };

        for (double[] testCase : testCases) {
            setScalarInput(0, testCase[0]);
            block.calculateOutput(0.0);

            double output = getScalarOutput(0);
            assertEquals("Gain should multiply " + testCase[0] + " by 2.0",
                testCase[1], output, DELTA);
        }
    }

    @Test
    public void testEdgeCases() {
        // Test: Edge case - Very small positive value
        setScalarInput(0, Double.MIN_VALUE);
        block.calculateOutput(0.0);
        double output = getScalarOutput(0);
        assertEquals("Gain should handle MIN_VALUE", 2 * Double.MIN_VALUE, output, 1e-300);

        // Test: Edge case - Just below zero
        setScalarInput(0, -1e-100);
        block.calculateOutput(0.0);
        output = getScalarOutput(0);
        assertEquals("Gain should handle tiny negative values", -2e-100, output, 1e-110);
    }
}