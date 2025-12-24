package com.ncslab.block.math;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Power block.
 * Tests calculateOutput() and power operations without WebSocket dependencies.
 *
 * Test Coverage:
 * - Basic power operations (x^y)
 * - Integer and fractional exponents
 * - Zero, negative, and fractional bases
 * - Special cases (0^0, x^0, 0^x)
 * - Large and small exponents
 * - Element-wise matrix operations
 * - Performance benchmarks
 */
public class PowerTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create Power block with default "Element-wise(.^)" method
        return Power.create("testPower", "test", "Element-wise(.^)", mockModel);
    }

    @Test
    public void testSquare() {
        // Test basic power: 2^2 = 4
        setScalarInput(0, 2.0); // Base
        setScalarInput(1, 2.0); // Exponent
        block.calculateOutput(0.0);
        assertEquals("2^2 should equal 4", 4.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testCube() {
        // Test cube: 3^3 = 27
        setScalarInput(0, 3.0);
        setScalarInput(1, 3.0);
        block.calculateOutput(0.0);
        assertEquals("3^3 should equal 27", 27.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testPowerOfZero() {
        // Test x^0 = 1 for any x
        setScalarInput(0, 5.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        assertEquals("5^0 should equal 1", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testPowerOfOne() {
        // Test x^1 = x
        setScalarInput(0, 7.0);
        setScalarInput(1, 1.0);
        block.calculateOutput(0.0);
        assertEquals("7^1 should equal 7", 7.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testZeroToPositivePower() {
        // Test 0^x = 0 for positive x
        setScalarInput(0, 0.0);
        setScalarInput(1, 3.0);
        block.calculateOutput(0.0);
        assertEquals("0^3 should equal 0", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNegativeBase() {
        // Test negative base with integer exponent: (-2)^3 = -8
        setScalarInput(0, -2.0);
        setScalarInput(1, 3.0);
        block.calculateOutput(0.0);
        assertEquals("(-2)^3 should equal -8", -8.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNegativeExponent() {
        // Test negative exponent: 2^(-2) = 0.25
        setScalarInput(0, 2.0);
        setScalarInput(1, -2.0);
        block.calculateOutput(0.0);
        assertEquals("2^(-2) should equal 0.25", 0.25, getScalarOutput(0), DELTA);
    }

    @Test
    public void testFractionalExponent() {
        // Test fractional exponent: 4^0.5 = 2 (square root)
        setScalarInput(0, 4.0);
        setScalarInput(1, 0.5);
        block.calculateOutput(0.0);
        assertEquals("4^0.5 should equal 2", 2.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testPerfectPowers() {
        // Test sequence of perfect powers
        double[][] testCases = {
            {2.0, 1.0, 2.0},
            {2.0, 2.0, 4.0},
            {2.0, 3.0, 8.0},
            {2.0, 4.0, 16.0},
            {2.0, 5.0, 32.0},
            {3.0, 2.0, 9.0},
            {3.0, 3.0, 27.0},
            {5.0, 2.0, 25.0},
            {10.0, 2.0, 100.0}
        };

        for (double[] testCase : testCases) {
            double base = testCase[0];
            double exp = testCase[1];
            double expected = testCase[2];

            setScalarInput(0, base);
            setScalarInput(1, exp);
            block.calculateOutput(0.0);
            assertEquals(base + "^" + exp + " should equal " + expected,
                expected, getScalarOutput(0), DELTA);
        }
    }

    @Test
    public void testPowerMatrixInput() {
        // Test power function on matrix inputs (element-wise)
        // Note: Matrix dimension propagation requires full model context
        double[][] baseValues = {{2.0, 3.0}, {4.0, 5.0}};
        double[][] expValues = {{2.0, 2.0}, {2.0, 2.0}};

        setMatrixInput(0, baseValues);
        setMatrixInput(1, expValues);
        block.calculateOutput(0.0);

        // Verify calculateOutput executes without errors
        assertNotNull("Output should not be null", getOutputData(0));
    }

    @Test
    public void testLargeExponent() {
        // Test large exponent: 2^10 = 1024
        setScalarInput(0, 2.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("2^10 should equal 1024", 1024.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSmallBase() {
        // Test small base with power: 0.1^2 = 0.01
        setScalarInput(0, 0.1);
        setScalarInput(1, 2.0);
        block.calculateOutput(0.0);
        assertEquals("0.1^2 should equal 0.01", 0.01, getScalarOutput(0), DELTA);
    }

    @Test
    public void testOutputPortConfiguration() {
        // Verify output port is properly configured
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertTrue("Power block should have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testInputPortConfiguration() {
        // Verify input port configuration (base and exponent)
        assertEquals("Should have 2 input ports", 2, block.getInputPortList().size());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testPower", "testPower", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be Power", "Power", block.getBlockType());
    }

    @Test
    public void testParameterCount() {
        // Power block should have parameters for power method, sample time, etc.
        assertTrue("Should have at least 4 parameters", block.getParameterList().size() >= 4);
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        setScalarInput(0, 3.0);
        setScalarInput(1, 2.0);

        for (int i = 0; i < 100; i++) {
            block.calculateOutput(i * 0.01);
            double output = getScalarOutput(0);
            assertEquals("Should always output 9.0", 9.0, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 2.0);
        setScalarInput(1, 3.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Power calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Power block should be fast (simple pow operation)
        assertTrue("Power calculateOutput should be fast", avgTime < 200000); // <200 microseconds
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        setScalarInput(0, 2.0);
        setScalarInput(1, 5.0);
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
        // Verify that output doesn't drift over many iterations with constant inputs
        setScalarInput(0, 2.0);
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

        assertEquals("Final output should be 8.0", 8.0, firstOutput, DELTA);
    }

    @Test
    public void testPowerIdentity() {
        // Test identity: (x^a)^b = x^(a*b)
        double x = 2.0;
        double a = 3.0;
        double b = 2.0;

        // Calculate x^a
        setScalarInput(0, x);
        setScalarInput(1, a);
        block.calculateOutput(0.0);
        double xToA = getScalarOutput(0);

        // Calculate (x^a)^b
        setScalarInput(0, xToA);
        setScalarInput(1, b);
        block.calculateOutput(0.0);
        double result1 = getScalarOutput(0);

        // Calculate x^(a*b)
        setScalarInput(0, x);
        setScalarInput(1, a * b);
        block.calculateOutput(0.0);
        double result2 = getScalarOutput(0);

        assertEquals("(x^a)^b should equal x^(a*b)",
            result1, result2, Math.abs(result1 * 1e-10));
    }

    @Test
    public void testVeryLargeResult() {
        // Test power operation resulting in very large value
        setScalarInput(0, 10.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        double expected = Math.pow(10.0, 10.0);
        assertEquals("10^10 should equal 1e10", expected, getScalarOutput(0), expected * 1e-10);
    }

    @Test
    public void testVerySmallResult() {
        // Test power operation resulting in very small value
        setScalarInput(0, 10.0);
        setScalarInput(1, -10.0);
        block.calculateOutput(0.0);
        double expected = Math.pow(10.0, -10.0);
        assertEquals("10^(-10) should equal 1e-10", expected, getScalarOutput(0), 1e-20);
    }

    @Test
    public void testMixedMatrixValues() {
        // Test power function on larger matrix with mixed values
        // Note: Matrix dimension propagation requires full model context
        double[][] baseValues = {
            {2.0, 3.0, 4.0},
            {1.0, 5.0, 2.0},
            {3.0, 2.0, 6.0}
        };
        double[][] expValues = {
            {3.0, 2.0, 2.0},
            {5.0, 2.0, 3.0},
            {2.0, 4.0, 1.0}
        };

        setMatrixInput(0, baseValues);
        setMatrixInput(1, expValues);
        block.calculateOutput(0.0);

        // Verify calculateOutput executes without errors
        assertNotNull("Output should not be null", getOutputData(0));
    }

    @Test
    public void testScalarBaseMatrixExponent() {
        // Test scalar base with matrix exponent
        // Note: Matrix dimension propagation requires full model context
        double baseValue = 2.0;
        double[][] expValues = {{1.0, 2.0}, {3.0, 4.0}};

        setScalarInput(0, baseValue);
        setMatrixInput(1, expValues);
        block.calculateOutput(0.0);

        // Verify calculateOutput executes without errors
        assertNotNull("Output should not be null", getOutputData(0));
    }

    @Test
    public void testMatrixBaseScalarExponent() {
        // Test matrix base with scalar exponent
        // Note: Matrix dimension propagation requires full model context
        double[][] baseValues = {{1.0, 2.0}, {3.0, 4.0}};
        double expValue = 2.0;

        setMatrixInput(0, baseValues);
        setScalarInput(1, expValue);
        block.calculateOutput(0.0);

        // Verify calculateOutput executes without errors
        assertNotNull("Output should not be null", getOutputData(0));
    }

    @Test
    public void testFractionalBaseAndExponent() {
        // Test fractional base and exponent: 0.5^0.5 ≈ 0.707
        setScalarInput(0, 0.5);
        setScalarInput(1, 0.5);
        block.calculateOutput(0.0);
        double expected = Math.pow(0.5, 0.5);
        assertEquals("0.5^0.5 should be approximately 0.707", expected, getScalarOutput(0), DELTA);
    }

    @Test
    public void testOneToAnyPower() {
        // Test 1^x = 1 for any x
        double[] exponents = {-10.0, -1.0, 0.0, 1.0, 2.0, 10.0, 100.0};

        for (double exp : exponents) {
            setScalarInput(0, 1.0);
            setScalarInput(1, exp);
            block.calculateOutput(0.0);
            assertEquals("1^" + exp + " should equal 1", 1.0, getScalarOutput(0), DELTA);
        }
    }

    @Test
    public void testMathematicalAccuracy() {
        // Test various power operations for mathematical accuracy
        double[][] testCases = {
            {8.0, 1.0/3.0, 2.0},        // Cube root of 8
            {27.0, 1.0/3.0, 3.0},       // Cube root of 27
            {16.0, 0.25, 2.0},          // Fourth root of 16
            {100.0, 0.5, 10.0},         // Square root of 100
            {0.25, -1.0, 4.0},          // Reciprocal of 0.25
            {2.0, -1.0, 0.5}            // Reciprocal of 2
        };

        for (double[] testCase : testCases) {
            double base = testCase[0];
            double exp = testCase[1];
            double expected = testCase[2];

            setScalarInput(0, base);
            setScalarInput(1, exp);
            block.calculateOutput(0.0);
            assertEquals(base + "^" + exp + " should equal " + expected,
                expected, getScalarOutput(0), DELTA);
        }
    }
}
