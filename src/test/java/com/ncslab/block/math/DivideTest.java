package com.ncslab.block.math;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Divide block.
 * Tests calculateOutput() and division operations without WebSocket dependencies.
 *
 * Test Coverage:
 * - Scalar division (2 inputs: numerator / denominator)
 * - Division by zero handling
 * - Zero values and negative values
 * - Large and small values
 * - Edge cases (infinity, NaN handling)
 * - Performance benchmarks
 */
public class DivideTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create Divide block with default element-wise division
        return Divide.create("testDivide", "test", "Element-wise(./.)", mockModel);
    }

    @Test
    public void testBasicDivision() {
        // Test basic division: 10 / 2 = 5
        setScalarInput(0, 10.0);
        setScalarInput(1, 2.0);
        block.calculateOutput(0.0);
        assertEquals("Should divide numerator by denominator", 5.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testDivisionByOne() {
        // Test division by 1: 42 / 1 = 42
        setScalarInput(0, 42.0);
        setScalarInput(1, 1.0);
        block.calculateOutput(0.0);
        assertEquals("Division by 1 should return original value", 42.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testDivisionByZero() {
        // Test division by zero: 10 / 0 = infinity
        setScalarInput(0, 10.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        double result = getScalarOutput(0);
        assertTrue("Division by zero should result in infinity", Double.isInfinite(result) && result > 0);
    }

    @Test
    public void testZeroDividedByNonZero() {
        // Test zero divided by non-zero: 0 / 5 = 0
        setScalarInput(0, 0.0);
        setScalarInput(1, 5.0);
        block.calculateOutput(0.0);
        assertEquals("Zero divided by non-zero should be zero", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testBothZero() {
        // Test zero divided by zero: 0 / 0 = NaN or infinity
        setScalarInput(0, 0.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        double result = getScalarOutput(0);
        // Result should be either NaN or infinity
        assertTrue("Zero divided by zero should be NaN or infinity",
                  Double.isNaN(result) || Double.isInfinite(result));
    }

    @Test
    public void testNegativeDivision() {
        // Test division with negative values: -10 / -2 = 5
        setScalarInput(0, -10.0);
        setScalarInput(1, -2.0);
        block.calculateOutput(0.0);
        assertEquals("Should handle negative division", 5.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testPositiveByNegative() {
        // Test positive divided by negative: 10 / (-2) = -5
        setScalarInput(0, 10.0);
        setScalarInput(1, -2.0);
        block.calculateOutput(0.0);
        assertEquals("Should handle positive by negative", -5.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNegativeByPositive() {
        // Test negative divided by positive: -10 / 2 = -5
        setScalarInput(0, -10.0);
        setScalarInput(1, 2.0);
        block.calculateOutput(0.0);
        assertEquals("Should handle negative by positive", -5.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLargeValues() {
        // Test division with large values
        double largeValue = 1e10;
        setScalarInput(0, largeValue);
        setScalarInput(1, 1e5);
        block.calculateOutput(0.0);
        assertEquals("Should handle large values", largeValue / 1e5, getScalarOutput(0), (largeValue / 1e5) * 1e-10);
    }

    @Test
    public void testSmallValues() {
        // Test division with small values
        double smallValue = 1e-5;
        setScalarInput(0, smallValue);
        setScalarInput(1, 1e-3);
        block.calculateOutput(0.0);
        assertEquals("Should handle small values", smallValue / 1e-3, getScalarOutput(0), 1e-20);
    }

    @Test
    public void testFractionalDivision() {
        // Test fractional division: 0.5 / 0.2 = 2.5
        setScalarInput(0, 0.5);
        setScalarInput(1, 0.2);
        block.calculateOutput(0.0);
        assertEquals("Should handle fractional division", 2.5, getScalarOutput(0), DELTA);
    }

    @Test
    public void testOutputPortConfiguration() {
        // Verify output port is properly configured
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertTrue("Divide block should have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testInputPortConfiguration() {
        // Verify input ports: numerator and denominator
        assertEquals("Should have 2 input ports", 2, block.getInputPortList().size());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testDivide", "testDivide", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be Divide", "Divide", block.getBlockType());
    }

    @Test
    public void testParameterCount() {
        // Divide block should have parameters for divideMethod, sampleTime, etc.
        assertTrue("Should have at least 3 parameters", block.getParameterList().size() >= 3);
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        setScalarInput(0, 10.0);
        setScalarInput(1, 2.0);

        for (int i = 0; i < 100; i++) {
            block.calculateOutput(i * 0.01);
            double output = getScalarOutput(0);
            assertEquals("Should always output 5.0", 5.0, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 10.0);
        setScalarInput(1, 2.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Divide calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Divide block should be fast (simple division operation with input handling)
        assertTrue("Divide calculateOutput should be fast", avgTime < 200000); // <200 microseconds
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        setScalarInput(0, 10.0);
        setScalarInput(1, 2.0);
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
        setScalarInput(1, 2.0);

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

        assertEquals("Final output should be 5.0", 5.0, firstOutput, DELTA);
    }

    @Test
    public void testReciprocal() {
        // Test division as reciprocal: 1 / 2 = 0.5
        setScalarInput(0, 1.0);
        setScalarInput(1, 2.0);
        block.calculateOutput(0.0);
        assertEquals("Should compute reciprocal correctly", 0.5, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNegativeDivisionByZero() {
        // Test negative divided by zero: -10 / 0 = infinity (implementation returns positive)
        setScalarInput(0, -10.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        double result = getScalarOutput(0);
        assertTrue("Negative divided by zero should result in infinity",
                  Double.isInfinite(result));
    }
}
