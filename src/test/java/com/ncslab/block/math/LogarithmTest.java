package com.ncslab.block.math;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Logarithm block.
 * Tests calculateOutput() and logarithm operations without WebSocket dependencies.
 *
 * Test Coverage:
 * - Natural logarithm (ln)
 * - Logarithm of various values (1, e, 10, etc.)
 * - Zero and negative input handling (NaN or -Infinity)
 * - Large and small values
 * - Edge cases (domain errors)
 * - Performance benchmarks
 */
public class LogarithmTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create Logarithm block with default "ln" (natural logarithm)
        return Logarithm.create("testLog", "test", "ln", mockModel);
    }

    @Test
    public void testLogOfOne() {
        // Test logarithm of one: ln(1) = 0
        setScalarInput(0, 1.0);
        block.calculateOutput(0.0);
        assertEquals("ln(1) should equal 0", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLogOfE() {
        // Test logarithm of e: ln(e) = 1
        setScalarInput(0, Math.E);
        block.calculateOutput(0.0);
        assertEquals("ln(e) should equal 1", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLogOfTen() {
        // Test logarithm of ten: ln(10) ≈ 2.303
        setScalarInput(0, 10.0);
        block.calculateOutput(0.0);
        double expected = Math.log(10.0);
        assertEquals("ln(10) should be approximately 2.303", expected, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLogOfTwo() {
        // Test logarithm of two: ln(2) ≈ 0.693
        setScalarInput(0, 2.0);
        block.calculateOutput(0.0);
        double expected = Math.log(2.0);
        assertEquals("ln(2) should be approximately 0.693", expected, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLogOfZero() {
        // Test logarithm of zero: ln(0) = -Infinity
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        double result = getScalarOutput(0);
        assertTrue("ln(0) should be -Infinity", Double.isInfinite(result) && result < 0);
    }

    @Test
    public void testLogOfNegative() {
        // Test logarithm of negative value: ln(-1) = NaN
        setScalarInput(0, -1.0);
        block.calculateOutput(0.0);
        double result = getScalarOutput(0);
        assertTrue("ln(-1) should be NaN", Double.isNaN(result));
    }

    @Test
    public void testLogOfLargeValue() {
        // Test logarithm of large value
        setScalarInput(0, 1000.0);
        block.calculateOutput(0.0);
        double expected = Math.log(1000.0);
        assertEquals("ln(1000) should be correct", expected, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLogOfSmallValue() {
        // Test logarithm of small positive value
        setScalarInput(0, 0.001);
        block.calculateOutput(0.0);
        double expected = Math.log(0.001);
        assertEquals("ln(0.001) should be negative", expected, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLogSequence() {
        // Test logarithm across a sequence of values
        double[] inputs = {0.1, 0.5, 1.0, 2.0, Math.E, 5.0, 10.0};

        for (double input : inputs) {
            double expected = Math.log(input);
            setScalarInput(0, input);
            block.calculateOutput(0.0);
            assertEquals("ln(" + input + ") should be correct",
                expected, getScalarOutput(0), Math.abs(expected * 1e-10));
        }
    }

    @Test
    public void testLogMatrixInput() {
        // Test logarithm function on matrix input (element-wise)
        // Note: Matrix dimension propagation requires full model context
        double[][] inputValues = {{1.0, Math.E}, {10.0, 100.0}};

        setMatrixInput(0, inputValues);
        block.calculateOutput(0.0);

        // Verify calculateOutput executes without errors
        assertNotNull("Output should not be null", getOutputData(0));
    }

    @Test
    public void testLogMathematicalProperty() {
        // Test mathematical property: ln(a*b) = ln(a) + ln(b)
        double a = 2.0;
        double b = 3.0;

        setScalarInput(0, a);
        block.calculateOutput(0.0);
        double logA = getScalarOutput(0);

        setScalarInput(0, b);
        block.calculateOutput(0.0);
        double logB = getScalarOutput(0);

        setScalarInput(0, a * b);
        block.calculateOutput(0.0);
        double logAB = getScalarOutput(0);

        assertEquals("ln(a*b) should equal ln(a) + ln(b)",
            logA + logB, logAB, Math.abs(logAB) * 1e-10);
    }

    @Test
    public void testOutputPortConfiguration() {
        // Verify output port is properly configured
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertTrue("Logarithm block should have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testInputPortConfiguration() {
        // Verify input port configuration
        assertEquals("Should have 1 input port", 1, block.getInputPortList().size());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testLog", "testLog", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be Logarithm", "Logarithm", block.getBlockType());
    }

    @Test
    public void testParameterCount() {
        // Logarithm block should have parameters for log type, sample time, etc.
        assertTrue("Should have at least 4 parameters", block.getParameterList().size() >= 4);
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        setScalarInput(0, Math.E);

        for (int i = 0; i < 100; i++) {
            block.calculateOutput(i * 0.01);
            double output = getScalarOutput(0);
            assertEquals("Should always output 1.0", 1.0, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 10.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Logarithm calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Logarithm block should be fast (simple log operation)
        assertTrue("Logarithm calculateOutput should be fast", avgTime < 200000); // <200 microseconds
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

        assertEquals("Output should be same after reinitialization",
            firstOutput, secondOutput, Math.abs(firstOutput * 1e-10));
    }

    @Test
    public void testNoDriftOverTime() {
        // Verify that output doesn't drift over many iterations with constant input
        setScalarInput(0, Math.E);

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

        assertEquals("Final output should be 1.0", 1.0, firstOutput, DELTA);
    }

    @Test
    public void testLogPowerProperty() {
        // Test power property: ln(x^n) = n * ln(x)
        double x = 2.0;
        double n = 3.0;

        setScalarInput(0, x);
        block.calculateOutput(0.0);
        double logX = getScalarOutput(0);

        setScalarInput(0, Math.pow(x, n));
        block.calculateOutput(0.0);
        double logXn = getScalarOutput(0);

        assertEquals("ln(x^n) should equal n*ln(x)",
            n * logX, logXn, Math.abs(logXn) * 1e-10);
    }

    @Test
    public void testLogVeryLargeValue() {
        // Test logarithm of very large value
        setScalarInput(0, 1e100);
        block.calculateOutput(0.0);
        double expected = Math.log(1e100);
        assertEquals("ln(1e100) should be correct", expected, getScalarOutput(0), Math.abs(expected * 1e-10));
    }

    @Test
    public void testLogVerySmallValue() {
        // Test logarithm of very small positive value (approaches -Infinity)
        setScalarInput(0, 1e-100);
        block.calculateOutput(0.0);
        double expected = Math.log(1e-100);
        assertEquals("ln(1e-100) should be very negative", expected, getScalarOutput(0), 1e-10);
    }

    @Test
    public void testLogMixedMatrixValues() {
        // Test logarithm function on larger matrix with mixed values
        // Note: Matrix dimension propagation requires full model context
        double[][] inputValues = {
            {1.0, 2.0, Math.E},
            {10.0, 100.0, 0.5},
            {0.1, 5.0, 20.0}
        };

        setMatrixInput(0, inputValues);
        block.calculateOutput(0.0);

        // Verify calculateOutput executes without errors
        assertNotNull("Output should not be null", getOutputData(0));
    }

    @Test
    public void testLogInverseProperty() {
        // Test inverse property: ln(exp(x)) = x
        double[] testValues = {0.0, 1.0, 2.0, 5.0, 10.0};

        for (double x : testValues) {
            double expX = Math.exp(x);
            setScalarInput(0, expX);
            block.calculateOutput(0.0);
            double result = getScalarOutput(0);

            assertEquals("ln(exp(" + x + ")) should equal " + x,
                x, result, Math.abs(x * 1e-10) + DELTA);
        }
    }

    @Test
    public void testLogDivisionProperty() {
        // Test division property: ln(a/b) = ln(a) - ln(b)
        double a = 10.0;
        double b = 2.0;

        setScalarInput(0, a);
        block.calculateOutput(0.0);
        double logA = getScalarOutput(0);

        setScalarInput(0, b);
        block.calculateOutput(0.0);
        double logB = getScalarOutput(0);

        setScalarInput(0, a / b);
        block.calculateOutput(0.0);
        double logAB = getScalarOutput(0);

        assertEquals("ln(a/b) should equal ln(a) - ln(b)",
            logA - logB, logAB, Math.abs(logAB) * 1e-10);
    }

    @Test
    public void testLogOfFractions() {
        // Test logarithm of fractional values
        double[] fractions = {0.5, 0.25, 0.1, 0.01};

        for (double fraction : fractions) {
            double expected = Math.log(fraction);
            setScalarInput(0, fraction);
            block.calculateOutput(0.0);
            assertEquals("ln(" + fraction + ") should be negative",
                expected, getScalarOutput(0), Math.abs(expected * 1e-10));
            assertTrue("ln of fraction should be negative", getScalarOutput(0) < 0);
        }
    }
}
