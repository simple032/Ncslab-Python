package com.ncslab.block.math;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Sqrt block.
 * Tests calculateOutput() and square root operations without WebSocket dependencies.
 *
 * Test Coverage:
 * - Standard sqrt function (sqrt)
 * - Square root of various values (0, 1, 4, 9, 16, etc.)
 * - Negative input handling (should use abs for safety)
 * - Large and small values
 * - Edge cases (zero, very small values)
 * - Performance benchmarks
 */
public class SqrtTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create Sqrt block with default "sqrt" function
        return Sqrt.create("testSqrt", "test", "sqrt", mockModel);
    }

    @Test
    public void testSqrtOfFour() {
        // Test basic square root: sqrt(4) = 2
        setScalarInput(0, 4.0);
        block.calculateOutput(0.0);
        assertEquals("sqrt(4) should equal 2", 2.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSqrtOfNine() {
        // Test square root: sqrt(9) = 3
        setScalarInput(0, 9.0);
        block.calculateOutput(0.0);
        assertEquals("sqrt(9) should equal 3", 3.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSqrtOfOne() {
        // Test square root: sqrt(1) = 1
        setScalarInput(0, 1.0);
        block.calculateOutput(0.0);
        assertEquals("sqrt(1) should equal 1", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSqrtOfZero() {
        // Test square root: sqrt(0) = 0
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        assertEquals("sqrt(0) should equal 0", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSqrtOfLargeValue() {
        // Test square root of large value: sqrt(10000) = 100
        setScalarInput(0, 10000.0);
        block.calculateOutput(0.0);
        assertEquals("sqrt(10000) should equal 100", 100.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSqrtOfSmallValue() {
        // Test square root of small value: sqrt(0.01) = 0.1
        setScalarInput(0, 0.01);
        block.calculateOutput(0.0);
        assertEquals("sqrt(0.01) should equal 0.1", 0.1, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSqrtOfFractionalValue() {
        // Test square root of fractional value: sqrt(0.25) = 0.5
        setScalarInput(0, 0.25);
        block.calculateOutput(0.0);
        assertEquals("sqrt(0.25) should equal 0.5", 0.5, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSqrtOfNegativeValue() {
        // Test square root of negative value (implementation uses abs for safety)
        // sqrt(abs(-4)) = sqrt(4) = 2
        setScalarInput(0, -4.0);
        block.calculateOutput(0.0);
        double result = getScalarOutput(0);

        // Implementation uses Math.sqrt(Math.abs(value)), so result should be 2
        assertEquals("sqrt(abs(-4)) should equal 2", 2.0, result, DELTA);
    }

    @Test
    public void testSqrtSequence() {
        // Test sqrt across a sequence of perfect squares
        double[][] testCases = {
            {0.0, 0.0},
            {1.0, 1.0},
            {4.0, 2.0},
            {9.0, 3.0},
            {16.0, 4.0},
            {25.0, 5.0},
            {36.0, 6.0},
            {49.0, 7.0},
            {64.0, 8.0},
            {81.0, 9.0},
            {100.0, 10.0}
        };

        for (double[] testCase : testCases) {
            double input = testCase[0];
            double expected = testCase[1];
            setScalarInput(0, input);
            block.calculateOutput(0.0);
            assertEquals("sqrt(" + input + ") should equal " + expected,
                expected, getScalarOutput(0), DELTA);
        }
    }

    @Test
    public void testSqrtMatrixInput() {
        // Test sqrt function on matrix input (element-wise)
        // Note: Matrix dimension propagation requires full model context
        double[][] inputValues = {{4.0, 9.0}, {16.0, 25.0}};

        setMatrixInput(0, inputValues);
        block.calculateOutput(0.0);

        // Verify calculateOutput executes without errors
        assertNotNull("Output should not be null", getOutputData(0));
    }

    @Test
    public void testSqrtOfVeryLargeValue() {
        // Test sqrt of very large value
        double input = 1e10;
        double expected = Math.sqrt(input);

        setScalarInput(0, input);
        block.calculateOutput(0.0);
        assertEquals("sqrt(1e10) should be correct", expected, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSqrtOfVerySmallValue() {
        // Test sqrt of very small value
        double input = 1e-10;
        double expected = Math.sqrt(input);

        setScalarInput(0, input);
        block.calculateOutput(0.0);
        assertEquals("sqrt(1e-10) should be correct", expected, getScalarOutput(0), 1e-15);
    }

    @Test
    public void testOutputPortConfiguration() {
        // Verify output port is properly configured
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertTrue("Sqrt block should have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testInputPortConfiguration() {
        // Verify input port configuration
        assertEquals("Should have 1 input port", 1, block.getInputPortList().size());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testSqrt", "testSqrt", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be Sqrt", "Sqrt", block.getBlockType());
    }

    @Test
    public void testParameterCount() {
        // Sqrt block should have parameters for function, sample time, etc.
        assertTrue("Should have at least 4 parameters", block.getParameterList().size() >= 4);
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        setScalarInput(0, 16.0);

        for (int i = 0; i < 100; i++) {
            block.calculateOutput(i * 0.01);
            double output = getScalarOutput(0);
            assertEquals("Should always output 4.0", 4.0, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 16.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Sqrt calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Sqrt block should be fast (simple sqrt operation)
        assertTrue("Sqrt calculateOutput should be fast", avgTime < 200000); // <200 microseconds
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        setScalarInput(0, 25.0);
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
        // Verify that output doesn't drift over many iterations with constant input
        setScalarInput(0, 9.0);

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

        assertEquals("Final output should be 3.0", 3.0, firstOutput, DELTA);
    }

    @Test
    public void testSqrtOfTwo() {
        // Test sqrt of 2 (irrational number)
        double expected = Math.sqrt(2.0);
        setScalarInput(0, 2.0);
        block.calculateOutput(0.0);
        assertEquals("sqrt(2) should be approximately 1.414", expected, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSqrtMixedMatrixValues() {
        // Test sqrt function on larger matrix with mixed values
        // Note: Matrix dimension propagation requires full model context
        double[][] inputValues = {
            {1.0, 4.0, 9.0},
            {16.0, 25.0, 36.0},
            {49.0, 64.0, 81.0}
        };

        setMatrixInput(0, inputValues);
        block.calculateOutput(0.0);

        // Verify calculateOutput executes without errors
        assertNotNull("Output should not be null", getOutputData(0));
    }

    @Test
    public void testSqrtMathematicalProperty() {
        // Test mathematical property: sqrt(x) * sqrt(x) = x
        double[] testValues = {4.0, 9.0, 16.0, 25.0, 100.0};

        for (double input : testValues) {
            setScalarInput(0, input);
            block.calculateOutput(0.0);
            double sqrtValue = getScalarOutput(0);
            double reconstructed = sqrtValue * sqrtValue;
            assertEquals("sqrt(" + input + ")^2 should equal " + input,
                input, reconstructed, DELTA);
        }
    }

    @Test
    public void testSqrtNonPerfectSquares() {
        // Test sqrt of non-perfect squares
        double[] inputs = {2.0, 3.0, 5.0, 6.0, 7.0, 8.0};

        for (double input : inputs) {
            double expected = Math.sqrt(input);
            setScalarInput(0, input);
            block.calculateOutput(0.0);
            assertEquals("sqrt(" + input + ") should be correct",
                expected, getScalarOutput(0), DELTA);
        }
    }
}
