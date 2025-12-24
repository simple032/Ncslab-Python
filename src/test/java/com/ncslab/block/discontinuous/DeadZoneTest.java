package com.ncslab.block.discontinuous;

import static org.junit.Assert.*;

import org.junit.Test;
import Jama.Matrix;

import com.ncslab.block.Block;
import com.ncslab.block.DirectBlockTestBase;

/**
 * Comprehensive test suite for DeadZone block using modern mock-based testing.
 *
 * Tests dead zone nonlinearity with configurable start and end thresholds.
 * Outputs zero within the dead zone and offset values outside.
 *
 * @author NCSLab Team
 * @version 2025
 */
public class DeadZoneTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Default: start=-0.5, end=0.5
        return DeadZone.create("testDeadZone", "test", "-0.5", "0.5", mockModel);
    }

    // ============================================================================
    // BASIC FUNCTIONALITY TESTS
    // ============================================================================

    @Test
    public void testInputWithinDeadZone() {
        setScalarInput(0, 0.25);
        block.calculateOutput(0.0);
        assertEquals("Input within dead zone should output zero",
            0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testInputBelowDeadZone() {
        setScalarInput(0, -1.0);
        block.calculateOutput(0.0);
        // Output = input - startOfDeadZone = -1.0 - (-0.5) = -0.5
        assertEquals("Input below dead zone should output offset value",
            -0.5, getScalarOutput(0), DELTA);
    }

    @Test
    public void testInputAboveDeadZone() {
        setScalarInput(0, 1.0);
        block.calculateOutput(0.0);
        // Output = input - endOfDeadZone = 1.0 - 0.5 = 0.5
        assertEquals("Input above dead zone should output offset value",
            0.5, getScalarOutput(0), DELTA);
    }

    @Test
    public void testInputAtStartThreshold() {
        setScalarInput(0, -0.5);
        block.calculateOutput(0.0);
        assertEquals("Input at start threshold should output zero",
            0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testInputAtEndThreshold() {
        setScalarInput(0, 0.5);
        block.calculateOutput(0.0);
        assertEquals("Input at end threshold should output zero",
            0.0, getScalarOutput(0), DELTA);
    }

    // ============================================================================
    // EDGE CASE TESTS
    // ============================================================================

    @Test
    public void testZeroInput() {
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        assertEquals("Zero input (within dead zone) should output zero",
            0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNearStartBoundary() {
        setScalarInput(0, -0.49);
        block.calculateOutput(0.0);
        assertEquals("Input near start boundary should output zero",
            0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNearEndBoundary() {
        setScalarInput(0, 0.49);
        block.calculateOutput(0.0);
        assertEquals("Input near end boundary should output zero",
            0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testJustBelowStartThreshold() {
        setScalarInput(0, -0.51);
        block.calculateOutput(0.0);
        // Output = -0.51 - (-0.5) = -0.01
        assertEquals("Input just below start threshold should output small offset",
            -0.01, getScalarOutput(0), DELTA);
    }

    @Test
    public void testJustAboveEndThreshold() {
        setScalarInput(0, 0.51);
        block.calculateOutput(0.0);
        // Output = 0.51 - 0.5 = 0.01
        assertEquals("Input just above end threshold should output small offset",
            0.01, getScalarOutput(0), DELTA);
    }

    @Test
    public void testVeryLargePositiveInput() {
        setScalarInput(0, 1000.0);
        block.calculateOutput(0.0);
        // Output = 1000.0 - 0.5 = 999.5
        assertEquals("Very large positive input should output large offset",
            999.5, getScalarOutput(0), DELTA);
    }

    @Test
    public void testVeryLargeNegativeInput() {
        setScalarInput(0, -1000.0);
        block.calculateOutput(0.0);
        // Output = -1000.0 - (-0.5) = -999.5
        assertEquals("Very large negative input should output large negative offset",
            -999.5, getScalarOutput(0), DELTA);
    }

    // ============================================================================
    // CUSTOM THRESHOLD TESTS
    // ============================================================================

    @Test
    public void testAsymmetricDeadZone() throws Exception {
        // Start=-1.0, End=2.0
        Block customBlock = DeadZone.create("testDeadZone", "test", "-1.0", "2.0", mockModel);
        initializeOutputSignals(customBlock);

        // Within dead zone
        setScalarInputForBlock(customBlock, 0, 0.5);
        customBlock.calculateOutput(0.0);
        assertEquals("Input within asymmetric dead zone should output zero",
            0.0, getScalarOutputForBlock(customBlock, 0), DELTA);

        // Below dead zone
        setScalarInputForBlock(customBlock, 0, -3.0);
        customBlock.calculateOutput(0.0);
        // Output = -3.0 - (-1.0) = -2.0
        assertEquals("Input below asymmetric dead zone should output offset",
            -2.0, getScalarOutputForBlock(customBlock, 0), DELTA);

        // Above dead zone
        setScalarInputForBlock(customBlock, 0, 5.0);
        customBlock.calculateOutput(0.0);
        // Output = 5.0 - 2.0 = 3.0
        assertEquals("Input above asymmetric dead zone should output offset",
            3.0, getScalarOutputForBlock(customBlock, 0), DELTA);
    }

    @Test
    public void testNarrowDeadZone() throws Exception {
        // Start=-0.1, End=0.1
        Block customBlock = DeadZone.create("testDeadZone", "test", "-0.1", "0.1", mockModel);
        initializeOutputSignals(customBlock);

        // Within narrow dead zone
        setScalarInputForBlock(customBlock, 0, 0.05);
        customBlock.calculateOutput(0.0);
        assertEquals("Input within narrow dead zone should output zero",
            0.0, getScalarOutputForBlock(customBlock, 0), DELTA);

        // Below narrow dead zone
        setScalarInputForBlock(customBlock, 0, -0.5);
        customBlock.calculateOutput(0.0);
        // Output = -0.5 - (-0.1) = -0.4
        assertEquals("Input below narrow dead zone should output offset",
            -0.4, getScalarOutputForBlock(customBlock, 0), DELTA);

        // Above narrow dead zone
        setScalarInputForBlock(customBlock, 0, 0.5);
        customBlock.calculateOutput(0.0);
        // Output = 0.5 - 0.1 = 0.4
        assertEquals("Input above narrow dead zone should output offset",
            0.4, getScalarOutputForBlock(customBlock, 0), DELTA);
    }

    @Test
    public void testWideDeadZone() throws Exception {
        // Start=-5.0, End=5.0
        Block customBlock = DeadZone.create("testDeadZone", "test", "-5.0", "5.0", mockModel);
        initializeOutputSignals(customBlock);

        // Within wide dead zone
        setScalarInputForBlock(customBlock, 0, 3.0);
        customBlock.calculateOutput(0.0);
        assertEquals("Input within wide dead zone should output zero",
            0.0, getScalarOutputForBlock(customBlock, 0), DELTA);

        // Below wide dead zone
        setScalarInputForBlock(customBlock, 0, -10.0);
        customBlock.calculateOutput(0.0);
        // Output = -10.0 - (-5.0) = -5.0
        assertEquals("Input below wide dead zone should output offset",
            -5.0, getScalarOutputForBlock(customBlock, 0), DELTA);

        // Above wide dead zone
        setScalarInputForBlock(customBlock, 0, 10.0);
        customBlock.calculateOutput(0.0);
        // Output = 10.0 - 5.0 = 5.0
        assertEquals("Input above wide dead zone should output offset",
            5.0, getScalarOutputForBlock(customBlock, 0), DELTA);
    }

    // ============================================================================
    // MATRIX INPUT TESTS
    // ============================================================================

    @Test
    public void testMatrixInputDeadZone() {
        double[][] input = {{-1.0, 0.25}, {0.75, -0.25}};
        // Expected: {(-1.0 - (-0.5)), 0.0}, {(0.75 - 0.5), 0.0}
        double[][] expected = {{-0.5, 0.0}, {0.25, 0.0}};

        setOutputDimensions(0, 2, 2); // Set output dimensions before setting input
        setMatrixInput(0, input);
        block.calculateOutput(0.0);

        assertMatrixOutput(0, expected);
    }

    @Test
    public void testMatrixAllWithinDeadZone() {
        double[][] input = {{-0.3, 0.2}, {0.4, -0.1}};
        double[][] expected = {{0.0, 0.0}, {0.0, 0.0}};

        setOutputDimensions(0, 2, 2); // Set output dimensions before setting input
        setMatrixInput(0, input);
        block.calculateOutput(0.0);

        assertMatrixOutput(0, expected);
    }

    @Test
    public void testMatrixAllBelowDeadZone() {
        double[][] input = {{-2.0, -1.0}, {-3.0, -0.6}};
        // Expected: {(-2.0 - (-0.5)), (-1.0 - (-0.5))}, {(-3.0 - (-0.5)), (-0.6 - (-0.5))}
        double[][] expected = {{-1.5, -0.5}, {-2.5, -0.1}};

        setOutputDimensions(0, 2, 2); // Set output dimensions before setting input
        setMatrixInput(0, input);
        block.calculateOutput(0.0);

        assertMatrixOutput(0, expected);
    }

    @Test
    public void testMatrixAllAboveDeadZone() {
        double[][] input = {{2.0, 1.0}, {3.0, 0.6}};
        // Expected: {(2.0 - 0.5), (1.0 - 0.5)}, {(3.0 - 0.5), (0.6 - 0.5)}
        double[][] expected = {{1.5, 0.5}, {2.5, 0.1}};

        setOutputDimensions(0, 2, 2); // Set output dimensions before setting input
        setMatrixInput(0, input);
        block.calculateOutput(0.0);

        assertMatrixOutput(0, expected);
    }

    // ============================================================================
    // MULTIPLE CALCULATION TESTS
    // ============================================================================

    @Test
    public void testMultipleCalculationsConsistency() {
        double[] testValues = {-2.0, -0.5, -0.25, 0.0, 0.25, 0.5, 2.0};
        double[] expectedOutputs = {-1.5, 0.0, 0.0, 0.0, 0.0, 0.0, 1.5};

        for (int i = 0; i < testValues.length; i++) {
            setScalarInput(0, testValues[i]);
            block.calculateOutput(0.0);
            assertEquals("Multiple calculations should be consistent for input " + testValues[i],
                expectedOutputs[i], getScalarOutput(0), DELTA);
        }
    }

    @Test
    public void testRapidValueChanges() {
        // Test dead zone behavior with rapid transitions
        double[] inputs = {-1.0, 1.0, 0.0, -1.0, 1.0};
        double[] expected = {-0.5, 0.5, 0.0, -0.5, 0.5};

        for (int i = 0; i < inputs.length; i++) {
            setScalarInput(0, inputs[i]);
            block.calculateOutput(0.0);
            assertEquals("Rapid value changes should work correctly",
                expected[i], getScalarOutput(0), DELTA);
        }
    }

    // ============================================================================
    // BLOCK METADATA TESTS
    // ============================================================================

    @Test
    public void testBlockName() {
        assertEquals("Block name should match", "testDeadZone", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be DeadZone", "DeadZone", block.getBlockType());
    }

    @Test
    public void testPortConfiguration() {
        assertEquals("Should have 1 input port", 1, block.getInputPortList().size());
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());
    }

    @Test
    public void testParameterCount() {
        // DeadZone has 5 parameters: LowerValue, UpperValue, SampleTime,
        // OutDataTypeStr, SaturateOnIntegerOverflow
        assertEquals("Should have 5 parameters", 5, block.getParameterList().size());
    }

    // ============================================================================
    // PERFORMANCE TESTS
    // ============================================================================

    @Test
    public void testCalculateOutputPerformance() {
        setScalarInput(0, 0.25);

        long avgTime = measureCalculateOutputPerformance(1000);

        // DeadZone should be very fast (< 200 microseconds)
        assertTrue("calculateOutput() should execute in less than 200 microseconds",
            avgTime < 200_000);
    }

    @Test
    public void testMatrixPerformance() {
        double[][] largeMatrix = new double[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                largeMatrix[i][j] = (i + j) * 0.1 - 0.5;
            }
        }

        setMatrixInput(0, largeMatrix);

        long startTime = System.nanoTime();
        block.calculateOutput(0.0);
        long endTime = System.nanoTime();

        long executionTime = endTime - startTime;
        System.out.println(String.format("Matrix dead zone (10x10) execution time: %.2f microseconds",
            executionTime / 1000.0));

        // Matrix dead zone should still be fast
        assertTrue("Matrix dead zone should execute in less than 500 microseconds",
            executionTime < 500_000);
    }

    // ============================================================================
    // REINITIALIZATION TEST
    // ============================================================================

    @Test
    public void testReinitialization() {
        setScalarInput(0, 1.0);
        block.calculateOutput(0.0);
        assertEquals("First calculation should output offset",
            0.5, getScalarOutput(0), DELTA);

        // Reinitialize
        try {
            block.calculateInit();
        } catch (Exception e) {
            // Initialization may not be needed for dead zone
        }

        setScalarInput(0, -1.0);
        block.calculateOutput(0.0);
        assertEquals("After reinitialization, should still work correctly",
            -0.5, getScalarOutput(0), DELTA);
    }

    // ============================================================================
    // NO DRIFT TEST
    // ============================================================================

    @Test
    public void testNoDriftOverTime() {
        // DeadZone should not accumulate error over time
        double input = 0.25;
        setScalarInput(0, input);

        for (int i = 0; i < 1000; i++) {
            block.calculateOutput(i * 0.01);
            assertEquals("Output should remain zero over 1000 iterations",
                0.0, getScalarOutput(0), DELTA);
        }
    }

    // ============================================================================
    // BOUNDARY PRECISION TESTS
    // ============================================================================

    @Test
    public void testPrecisionAtBoundaries() {
        // Test precision near boundaries
        double epsilon = 1e-10;

        // Just inside start boundary
        setScalarInput(0, -0.5 + epsilon);
        block.calculateOutput(0.0);
        assertEquals("Just inside start boundary should output zero",
            0.0, getScalarOutput(0), DELTA);

        // Just outside start boundary
        setScalarInput(0, -0.5 - epsilon);
        block.calculateOutput(0.0);
        assertTrue("Just outside start boundary should output small negative value",
            getScalarOutput(0) < 0.0);

        // Just inside end boundary
        setScalarInput(0, 0.5 - epsilon);
        block.calculateOutput(0.0);
        assertEquals("Just inside end boundary should output zero",
            0.0, getScalarOutput(0), DELTA);

        // Just outside end boundary
        setScalarInput(0, 0.5 + epsilon);
        block.calculateOutput(0.0);
        assertTrue("Just outside end boundary should output small positive value",
            getScalarOutput(0) > 0.0);
    }
}
