package com.ncslab.block.math;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Abs (Absolute Value) block.
 * Tests calculateOutput() directly without WebSocket dependencies.
 *
 * Test Coverage:
 * - Scalar absolute value calculation
 * - Matrix element-wise absolute value
 * - Edge cases (zeros, negatives, NaN, Infinity)
 * - Performance benchmarks
 */
public class AbsTest extends DirectBlockTestBase {

    private Abs absBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create Abs block
        absBlock = Abs.create("testAbs", "test", mockModel);
        return absBlock;
    }

    @Test
    public void testPositiveInput() {
        // Test: |5.0| = 5.0
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Abs of positive should remain positive", 5.0, output, DELTA);
    }

    @Test
    public void testNegativeInput() {
        // Test: |-5.0| = 5.0
        setScalarInput(0, -5.0);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Abs of negative should be positive", 5.0, output, DELTA);
    }

    @Test
    public void testZeroInput() {
        // Test: |0.0| = 0.0
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Abs of zero should be zero", 0.0, output, DELTA);
    }

    @Test
    public void testNegativeZeroInput() {
        // Test: |-0.0| = 0.0
        setScalarInput(0, -0.0);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Abs of negative zero should be zero", 0.0, output, DELTA);
        assertTrue("Abs of -0.0 should be +0.0", Double.compare(output, +0.0) == 0);
    }

    @Test
    public void testLargePositiveValue() {
        // Test: |1e10| = 1e10
        setScalarInput(0, 1e10);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Abs of large positive", 1e10, output, 1.0);
    }

    @Test
    public void testLargeNegativeValue() {
        // Test: |-1e10| = 1e10
        setScalarInput(0, -1e10);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Abs of large negative", 1e10, output, 1.0);
    }

    @Test
    public void testSmallPositiveValue() {
        // Test: |1e-10| = 1e-10
        setScalarInput(0, 1e-10);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Abs of small positive", 1e-10, output, 1e-20);
    }

    @Test
    public void testSmallNegativeValue() {
        // Test: |-1e-10| = 1e-10
        setScalarInput(0, -1e-10);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Abs of small negative", 1e-10, output, 1e-20);
    }

    @Test
    public void testMatrixInput() {
        // Test: Matrix element-wise absolute value
        double[][] inputMatrix = {{1.0, -2.0}, {-3.0, 4.0}};
        double[][] expectedMatrix = {{1.0, 2.0}, {3.0, 4.0}};

        setMatrixInput(0, inputMatrix);
        block.calculateOutput(0.0);

        assertMatrixOutput(0, expectedMatrix);
    }

    @Test
    public void testMatrixAllNegative() {
        // Test: Matrix with all negative values
        double[][] inputMatrix = {{-1.0, -2.0}, {-3.0, -4.0}};
        double[][] expectedMatrix = {{1.0, 2.0}, {3.0, 4.0}};

        setMatrixInput(0, inputMatrix);
        block.calculateOutput(0.0);

        assertMatrixOutput(0, expectedMatrix);
    }

    @Test
    public void testMatrixAllPositive() {
        // Test: Matrix with all positive values
        double[][] inputMatrix = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] expectedMatrix = {{1.0, 2.0}, {3.0, 4.0}};

        setMatrixInput(0, inputMatrix);
        block.calculateOutput(0.0);

        assertMatrixOutput(0, expectedMatrix);
    }

    @Test
    public void testMatrixWithZeros() {
        // Test: Matrix containing zeros
        double[][] inputMatrix = {{0.0, -2.0}, {3.0, 0.0}};
        double[][] expectedMatrix = {{0.0, 2.0}, {3.0, 0.0}};

        setMatrixInput(0, inputMatrix);
        block.calculateOutput(0.0);

        assertMatrixOutput(0, expectedMatrix);
    }

    @Test
    public void testNaNInput() {
        // Test: NaN input should produce NaN output
        setScalarInput(0, Double.NaN);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertTrue("Abs of NaN should be NaN", Double.isNaN(output));
    }

    @Test
    public void testPositiveInfinityInput() {
        // Test: |+Infinity| = +Infinity
        setScalarInput(0, Double.POSITIVE_INFINITY);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Abs of +Infinity should be +Infinity",
                    Double.POSITIVE_INFINITY, output, DELTA);
    }

    @Test
    public void testNegativeInfinityInput() {
        // Test: |-Infinity| = +Infinity
        setScalarInput(0, Double.NEGATIVE_INFINITY);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Abs of -Infinity should be +Infinity",
                    Double.POSITIVE_INFINITY, output, DELTA);
    }

    @Test
    public void testMultipleCalculations() {
        // Test: Multiple calculations with different inputs
        double[][] testCases = {
            {5.0, 5.0},
            {-5.0, 5.0},
            {0.0, 0.0},
            {-0.0, 0.0},
            {100.0, 100.0},
            {-100.0, 100.0},
            {1e-5, 1e-5},
            {-1e-5, 1e-5}
        };

        for (double[] testCase : testCases) {
            setScalarInput(0, testCase[0]);
            block.calculateOutput(0.0);

            double output = getScalarOutput(0);
            assertEquals("Abs(" + testCase[0] + ") should be " + testCase[1],
                        testCase[1], output, DELTA);
        }
    }

    @Test
    public void testRepeatedCalculation() {
        // Test: Same input calculated multiple times
        setScalarInput(0, -7.5);

        block.calculateOutput(0.0);
        double firstOutput = getScalarOutput(0);

        block.calculateOutput(0.1);
        double secondOutput = getScalarOutput(0);

        block.calculateOutput(0.2);
        double thirdOutput = getScalarOutput(0);

        assertEquals("First output should be 7.5", 7.5, firstOutput, DELTA);
        assertEquals("Outputs should be consistent", firstOutput, secondOutput, DELTA);
        assertEquals("Outputs should be consistent", firstOutput, thirdOutput, DELTA);
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, -5.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Abs calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion
        assertTrue("Abs calculateOutput should be fast", avgTime < 10000);
    }

    @Test
    public void testPortConfiguration() {
        // Test: Abs should have 1 input and 1 output
        assertEquals("Should have 1 input port", 1, block.getInputPortList().size());
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());
    }

    @Test
    public void testFeedthrough() {
        // Test: Abs is instantaneous, should have feedthrough
        assertTrue("Abs should have feedthrough",
                  block.getOutputPortList().get(0).getFeedThrough());
    }

    @Test
    public void testNoStates() {
        // Test: Abs block should have no states (it's a pure function)
        assertEquals("Abs should have 0 states", 0, block.getStateList().size());
    }

    @Test
    public void testEdgeCases_MaxValue() {
        // Test: |Double.MAX_VALUE| = Double.MAX_VALUE
        setScalarInput(0, Double.MAX_VALUE);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Abs of MAX_VALUE", Double.MAX_VALUE, output, Double.MAX_VALUE * 1e-10);
    }

    @Test
    public void testEdgeCases_MinValue() {
        // Test: |-Double.MAX_VALUE| = Double.MAX_VALUE
        setScalarInput(0, -Double.MAX_VALUE);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Abs of -MAX_VALUE", Double.MAX_VALUE, output, Double.MAX_VALUE * 1e-10);
    }

    @Test
    public void testEdgeCases_MinNormalValue() {
        // Test: |Double.MIN_NORMAL| = Double.MIN_NORMAL
        setScalarInput(0, Double.MIN_NORMAL);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Abs of MIN_NORMAL", Double.MIN_NORMAL, output, 1e-300);
    }

    @Test
    public void testEdgeCases_SubnormalValue() {
        // Test: Very small subnormal values
        double subnormal = Double.MIN_VALUE * 10;
        setScalarInput(0, -subnormal);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Abs of subnormal", subnormal, output, 1e-300);
    }

    @Test
    public void testSymmetry() {
        // Test: Abs(-x) should equal Abs(x)
        double[] testValues = {1.0, 5.0, 10.0, 100.0, 1e-5, 1e5};

        for (double value : testValues) {
            setScalarInput(0, value);
            block.calculateOutput(0.0);
            double positiveResult = getScalarOutput(0);

            setScalarInput(0, -value);
            block.calculateOutput(0.0);
            double negativeResult = getScalarOutput(0);

            assertEquals("Abs(" + value + ") should equal Abs(-" + value + ")",
                        positiveResult, negativeResult, DELTA);
        }
    }

    @Test
    public void testIdempotence() {
        // Test: Abs(Abs(x)) = Abs(x)
        // Since we can't chain blocks directly in this test, we verify that
        // applying Abs to a positive value (result of Abs) doesn't change it

        setScalarInput(0, -5.0);
        block.calculateOutput(0.0);
        double firstAbs = getScalarOutput(0);

        // Set the result as input again
        setScalarInput(0, firstAbs);
        block.calculateOutput(0.0);
        double secondAbs = getScalarOutput(0);

        assertEquals("Abs should be idempotent", firstAbs, secondAbs, DELTA);
        assertEquals("Both should be 5.0", 5.0, secondAbs, DELTA);
    }

    @Test
    public void testMonotonicity() {
        // Test: For x >= 0, Abs is monotonically increasing
        double[] positiveValues = {0.0, 1.0, 2.0, 5.0, 10.0};
        double previousOutput = -1.0;

        for (double value : positiveValues) {
            setScalarInput(0, value);
            block.calculateOutput(0.0);
            double output = getScalarOutput(0);

            assertTrue("Abs should be monotonically increasing for positive values",
                      output >= previousOutput);
            previousOutput = output;
        }
    }

    @Test
    public void testPrecision() {
        // Test: Precision with values close to zero
        double[] precisionTests = {1e-1, 1e-2, 1e-3, 1e-4, 1e-5, 1e-6};

        for (double value : precisionTests) {
            setScalarInput(0, -value);
            block.calculateOutput(0.0);
            double output = getScalarOutput(0);

            assertEquals("Abs(-" + value + ") should equal " + value,
                        value, output, value * 1e-10);
        }
    }
}
