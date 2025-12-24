package com.ncslab.block.discontinuous;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Saturation block.
 * Tests calculateOutput() directly without WebSocket dependencies.
 */
public class SaturationTest extends DirectBlockTestBase {

    private Saturation saturationBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create Saturation block with limits [-1.0, 1.0]
        saturationBlock = Saturation.create("testSaturation", "test", "1.0", "-1.0", mockModel);
        return saturationBlock;
    }

    @Test
    public void testNoSaturation() {
        // Test: Input within limits should pass through unchanged
        setScalarInput(0, 0.5);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Value within limits should pass through", 0.5, output, DELTA);
    }

    @Test
    public void testUpperSaturation() {
        // Test: Input above upper limit should be saturated
        setScalarInput(0, 2.0);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Value above upper limit should be saturated to 1.0", 1.0, output, DELTA);
    }

    @Test
    public void testLowerSaturation() {
        // Test: Input below lower limit should be saturated
        setScalarInput(0, -2.0);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Value below lower limit should be saturated to -1.0", -1.0, output, DELTA);
    }

    @Test
    public void testExactUpperLimit() {
        // Test: Input exactly at upper limit
        setScalarInput(0, 1.0);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Value at upper limit should not be saturated", 1.0, output, DELTA);
    }

    @Test
    public void testExactLowerLimit() {
        // Test: Input exactly at lower limit
        setScalarInput(0, -1.0);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Value at lower limit should not be saturated", -1.0, output, DELTA);
    }

    @Test
    public void testZeroInput() {
        // Test: Zero input
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Zero input should pass through", 0.0, output, DELTA);
    }

    @Test
    public void testMatrixSaturation() {
        // Test: Matrix input with saturation
        double[][] inputMatrix = {{-2.0, 0.5}, {1.5, -0.5}};
        setMatrixInput(0, inputMatrix);
        block.calculateOutput(0.0);

        double[][] expectedMatrix = {{-1.0, 0.5}, {1.0, -0.5}};
        assertMatrixOutput(0, expectedMatrix);
    }

    @Test
    public void testAsymmetricLimits() throws Exception {
        // Create saturation with asymmetric limits
        Saturation asymSat = Saturation.create("asymSat", "test", "5.0", "-2.0", mockModel);
        asymSat.calculateInit();

        // Test upper saturation
        mockInputPortConnection(asymSat, 0, new com.ncslab.block.data.Data(10.0));
        asymSat.calculateOutput(0.0);
        double output = asymSat.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        assertEquals("Should saturate to upper limit 5.0", 5.0, output, DELTA);

        // Test lower saturation
        mockInputPortConnection(asymSat, 0, new com.ncslab.block.data.Data(-10.0));
        asymSat.calculateOutput(0.0);
        output = asymSat.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        assertEquals("Should saturate to lower limit -2.0", -2.0, output, DELTA);
    }

    @Test
    public void testLargePositiveValues() {
        // Test: Very large positive values
        setScalarInput(0, 1e10);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Large positive value should saturate to 1.0", 1.0, output, DELTA);
    }

    @Test
    public void testLargeNegativeValues() {
        // Test: Very large negative values
        setScalarInput(0, -1e10);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Large negative value should saturate to -1.0", -1.0, output, DELTA);
    }

    @Test
    public void testMultipleSaturations() {
        // Test: Multiple calculations with different inputs
        double[][] testCases = {
            {2.0, 1.0},      // Upper saturation
            {-2.0, -1.0},    // Lower saturation
            {0.0, 0.0},      // No saturation
            {0.9, 0.9},      // No saturation
            {-0.9, -0.9},    // No saturation
            {1.1, 1.0},      // Upper saturation
            {-1.1, -1.0}     // Lower saturation
        };

        for (double[] testCase : testCases) {
            setScalarInput(0, testCase[0]);
            block.calculateOutput(0.0);

            double output = getScalarOutput(0);
            assertEquals("Input " + testCase[0] + " should saturate to " + testCase[1],
                testCase[1], output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 2.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Saturation calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 10 microseconds on average
        assertTrue("Saturation calculateOutput should be fast", avgTime < 10000);
    }

    @Test
    public void testInfiniteUpperLimit() throws Exception {
        // Create saturation with infinite upper limit
        Saturation infSat = Saturation.create("infSat", "test", "Inf", "-1.0", mockModel);
        infSat.calculateInit();

        // Test that large positive values pass through
        mockInputPortConnection(infSat, 0, new com.ncslab.block.data.Data(1e10));
        infSat.calculateOutput(0.0);
        double output = infSat.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        assertEquals("Large positive value should pass through with infinite upper limit", 1e10, output, 1e-4);

        // Test that lower saturation still works
        mockInputPortConnection(infSat, 0, new com.ncslab.block.data.Data(-2.0));
        infSat.calculateOutput(0.0);
        output = infSat.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        assertEquals("Should still saturate to lower limit", -1.0, output, DELTA);
    }

    @Test
    public void testSymmetricZeroLimits() throws Exception {
        // Create saturation with symmetric limits around zero
        Saturation symSat = Saturation.create("symSat", "test", "10.0", "-10.0", mockModel);
        symSat.calculateInit();

        // Test values within range
        double[] testInputs = {0.0, 5.0, -5.0, 9.9, -9.9};
        double[] expectedOutputs = {0.0, 5.0, -5.0, 9.9, -9.9};

        for (int i = 0; i < testInputs.length; i++) {
            mockInputPortConnection(symSat, 0, new com.ncslab.block.data.Data(testInputs[i]));
            symSat.calculateOutput(0.0);
            double output = symSat.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
            assertEquals("Input " + testInputs[i] + " should pass through",
                expectedOutputs[i], output, DELTA);
        }
    }
}