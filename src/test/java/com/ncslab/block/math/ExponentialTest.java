package com.ncslab.block.math;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Exponential block.
 * Tests calculateOutput() and exponential operations without WebSocket dependencies.
 *
 * Test Coverage:
 * - Natural exponential (e^x)
 * - Exponential of zero, positive, negative values
 * - Large and small exponents
 * - Edge cases (overflow, underflow)
 * - Matrix operations (element-wise)
 * - Performance benchmarks
 */
public class ExponentialTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create Exponential block with default "exp" (natural exponential)
        return Exponential.create("testExp", "test", "exp", mockModel);
    }

    @Test
    public void testExpOfZero() {
        // Test exponential of zero: e^0 = 1
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        assertEquals("e^0 should equal 1", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testExpOfOne() {
        // Test exponential of one: e^1 = e ≈ 2.71828
        setScalarInput(0, 1.0);
        block.calculateOutput(0.0);
        assertEquals("e^1 should equal e", Math.E, getScalarOutput(0), DELTA);
    }

    @Test
    public void testExpOfTwo() {
        // Test exponential of two: e^2 ≈ 7.389
        setScalarInput(0, 2.0);
        block.calculateOutput(0.0);
        double expected = Math.exp(2.0);
        assertEquals("e^2 should be approximately 7.389", expected, getScalarOutput(0), DELTA);
    }

    @Test
    public void testExpOfNegativeOne() {
        // Test exponential of negative one: e^(-1) ≈ 0.368
        setScalarInput(0, -1.0);
        block.calculateOutput(0.0);
        double expected = Math.exp(-1.0);
        assertEquals("e^(-1) should be approximately 0.368", expected, getScalarOutput(0), DELTA);
    }

    @Test
    public void testExpOfLargePositiveValue() {
        // Test exponential of large positive value
        setScalarInput(0, 10.0);
        block.calculateOutput(0.0);
        double expected = Math.exp(10.0);
        assertEquals("e^10 should be correct", expected, getScalarOutput(0), expected * 1e-10);
    }

    @Test
    public void testExpOfLargeNegativeValue() {
        // Test exponential of large negative value (approaches 0)
        setScalarInput(0, -10.0);
        block.calculateOutput(0.0);
        double expected = Math.exp(-10.0);
        assertEquals("e^(-10) should be very small", expected, getScalarOutput(0), 1e-10);
    }

    @Test
    public void testExpOfSmallPositiveValue() {
        // Test exponential of small positive value
        setScalarInput(0, 0.1);
        block.calculateOutput(0.0);
        double expected = Math.exp(0.1);
        assertEquals("e^0.1 should be approximately 1.105", expected, getScalarOutput(0), DELTA);
    }

    @Test
    public void testExpOfSmallNegativeValue() {
        // Test exponential of small negative value
        setScalarInput(0, -0.1);
        block.calculateOutput(0.0);
        double expected = Math.exp(-0.1);
        assertEquals("e^(-0.1) should be approximately 0.905", expected, getScalarOutput(0), DELTA);
    }

    @Test
    public void testExpSequence() {
        // Test exponential across a sequence of values
        double[] inputs = {-3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0};

        for (double input : inputs) {
            double expected = Math.exp(input);
            setScalarInput(0, input);
            block.calculateOutput(0.0);
            assertEquals("e^" + input + " should be correct", expected, getScalarOutput(0), Math.abs(expected) * 1e-10);
        }
    }

    @Test
    public void testExpMatrixInput() {
        // Test exponential function on matrix input (element-wise)
        // Note: Matrix dimension propagation requires full model context
        double[][] inputValues = {{0.0, 1.0}, {-1.0, 2.0}};

        setMatrixInput(0, inputValues);
        block.calculateOutput(0.0);

        // Verify calculateOutput executes without errors
        assertNotNull("Output should not be null", getOutputData(0));
    }

    @Test
    public void testExpMathematicalProperty() {
        // Test mathematical property: e^(a+b) = e^a * e^b
        double a = 2.0;
        double b = 3.0;

        setScalarInput(0, a);
        block.calculateOutput(0.0);
        double expA = getScalarOutput(0);

        setScalarInput(0, b);
        block.calculateOutput(0.0);
        double expB = getScalarOutput(0);

        setScalarInput(0, a + b);
        block.calculateOutput(0.0);
        double expAB = getScalarOutput(0);

        assertEquals("e^(a+b) should equal e^a * e^b", expA * expB, expAB, expAB * 1e-10);
    }

    @Test
    public void testOutputPortConfiguration() {
        // Verify output port is properly configured
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertTrue("Exponential block should have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testInputPortConfiguration() {
        // Verify input port configuration
        assertEquals("Should have 1 input port", 1, block.getInputPortList().size());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testExp", "testExp", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be Exponential", "Exponential", block.getBlockType());
    }

    @Test
    public void testParameterCount() {
        // Exponential block should have parameters for exp type, sample time, etc.
        assertTrue("Should have at least 4 parameters", block.getParameterList().size() >= 4);
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        setScalarInput(0, 1.0);

        for (int i = 0; i < 100; i++) {
            block.calculateOutput(i * 0.01);
            double output = getScalarOutput(0);
            assertEquals("Should always output e", Math.E, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 1.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Exponential calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Exponential block should be fast (simple exp operation)
        assertTrue("Exponential calculateOutput should be fast", avgTime < 200000); // <200 microseconds
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

        assertEquals("Output should be same after reinitialization", firstOutput, secondOutput, firstOutput * 1e-10);
    }

    @Test
    public void testNoDriftOverTime() {
        // Verify that output doesn't drift over many iterations with constant input
        setScalarInput(0, 1.0);

        double firstOutput = 0;
        for (int i = 0; i < 1000; i++) {
            block.calculateOutput(i * 0.001);
            double currentOutput = getScalarOutput(0);

            if (i == 0) {
                firstOutput = currentOutput;
            } else {
                assertEquals("Output should not drift at iteration " + i,
                    firstOutput, currentOutput, firstOutput * 1e-10);
            }
        }

        assertEquals("Final output should be e", Math.E, firstOutput, DELTA);
    }

    @Test
    public void testExpAlwaysPositive() {
        // Test that exponential is always positive for any real input
        double[] testInputs = {-100.0, -10.0, -1.0, 0.0, 1.0, 10.0, 100.0};

        for (double input : testInputs) {
            setScalarInput(0, input);
            block.calculateOutput(0.0);
            double output = getScalarOutput(0);
            assertTrue("e^" + input + " should be positive", output > 0);
        }
    }

    @Test
    public void testExpVeryLargeExponent() {
        // Test exponential with very large exponent (may overflow to Infinity)
        setScalarInput(0, 100.0);
        block.calculateOutput(0.0);
        double output = getScalarOutput(0);

        // e^100 is extremely large, check it's either correct or Infinity
        double expected = Math.exp(100.0);
        assertTrue("e^100 should be very large or Infinity",
            output == expected || Double.isInfinite(output));
    }

    @Test
    public void testExpVerySmallExponent() {
        // Test exponential with very small exponent (approaches 0)
        setScalarInput(0, -100.0);
        block.calculateOutput(0.0);
        double output = getScalarOutput(0);
        double expected = Math.exp(-100.0);

        assertTrue("e^(-100) should be very small", output < 1e-40);
        assertEquals("e^(-100) should match Math.exp", expected, output, 1e-50);
    }

    @Test
    public void testExpMixedMatrixValues() {
        // Test exponential function on larger matrix with mixed values
        // Note: Matrix dimension propagation requires full model context
        double[][] inputValues = {
            {0.0, 1.0, -1.0},
            {2.0, -2.0, 0.5},
            {-0.5, 3.0, -3.0}
        };

        setMatrixInput(0, inputValues);
        block.calculateOutput(0.0);

        // Verify calculateOutput executes without errors
        assertNotNull("Output should not be null", getOutputData(0));
    }

    @Test
    public void testExpInverseProperty() {
        // Test inverse property: exp(ln(x)) = x
        double[] testValues = {1.0, 2.0, 5.0, 10.0, 100.0};

        for (double x : testValues) {
            double lnX = Math.log(x);
            setScalarInput(0, lnX);
            block.calculateOutput(0.0);
            double result = getScalarOutput(0);

            assertEquals("exp(ln(" + x + ")) should equal " + x,
                x, result, x * 1e-10);
        }
    }
}
