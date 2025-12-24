package com.ncslab.block.source;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Ramp block.
 * Tests calculateInit() and output generation without WebSocket dependencies.
 *
 * Test Coverage:
 * - Ramp output before start time (initial output)
 * - Ramp output at and after start time (slope-based)
 * - Various slope values (positive, negative, zero)
 * - Various start times
 * - Various initial output values
 * - Edge cases (zero slope, negative slope, large values)
 * - Performance benchmarks
 */
public class RampTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create Ramp block: slope=1.0, start=0.0, initialOutput=0.0
        return Ramp.create("testRamp", "test", 1.0, 0.0, 0.0, mockModel);
    }

    @Test
    public void testBeforeStartTime() {
        // Test output before ramp starts
        block.calculateOutput(0.0);
        assertEquals("Should output initial value before start", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testAtStartTime() {
        // Test output exactly at start time
        block.calculateOutput(0.0);
        assertEquals("Should output initial value at start time", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testAfterStartTime() {
        // Test output after ramp starts (slope * (t - start) + initialOutput)
        block.calculateOutput(5.0);
        assertEquals("Should output ramp value after start", 5.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testMultipleTimePoints() {
        // Verify ramp behavior across multiple time points
        // slope=1.0, start=0.0, initialOutput=0.0
        // output = t >= 0 ? 1.0 * (t - 0.0) + 0.0 : 0.0
        double[][] testCases = {
            {0.0, 0.0},    // t=0.0 → 0.0
            {1.0, 1.0},    // t=1.0 → 1.0
            {2.0, 2.0},    // t=2.0 → 2.0
            {5.0, 5.0},    // t=5.0 → 5.0
            {10.0, 10.0}   // t=10.0 → 10.0
        };

        for (double[] testCase : testCases) {
            double time = testCase[0];
            double expected = testCase[1];
            block.calculateOutput(time);
            assertEquals("Ramp output mismatch at t=" + time, expected, getScalarOutput(0), DELTA);
        }
    }

    @Test
    public void testPositiveSlope() throws Exception {
        // Test ramp with positive slope
        Ramp positiveRamp = Ramp.create("positiveRamp", "test", 2.0, 1.0, 5.0, mockModel);
        initializeOutputSignals(positiveRamp);
        positiveRamp.calculateInit();

        // Before start time
        positiveRamp.calculateOutput(0.5);
        assertEquals("Should output initial value before start", 5.0,
            positiveRamp.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // At start time
        positiveRamp.calculateOutput(1.0);
        assertEquals("Should output initial value at start", 5.0,
            positiveRamp.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // After start time: output = 2.0 * (3.0 - 1.0) + 5.0 = 9.0
        positiveRamp.calculateOutput(3.0);
        assertEquals("Should output ramp value after start", 9.0,
            positiveRamp.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testNegativeSlope() throws Exception {
        // Test ramp with negative slope
        Ramp negativeRamp = Ramp.create("negativeRamp", "test", -1.0, 2.0, 10.0, mockModel);
        initializeOutputSignals(negativeRamp);
        negativeRamp.calculateInit();

        // Before start time
        negativeRamp.calculateOutput(1.0);
        assertEquals("Should output initial value before start", 10.0,
            negativeRamp.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // At start time
        negativeRamp.calculateOutput(2.0);
        assertEquals("Should output initial value at start", 10.0,
            negativeRamp.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // After start time: output = -1.0 * (5.0 - 2.0) + 10.0 = 7.0
        negativeRamp.calculateOutput(5.0);
        assertEquals("Should output ramp value after start", 7.0,
            negativeRamp.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testZeroSlope() throws Exception {
        // Test ramp with zero slope (constant output)
        Ramp zeroSlope = Ramp.create("zeroSlope", "test", 0.0, 1.0, 5.0, mockModel);
        initializeOutputSignals(zeroSlope);
        zeroSlope.calculateInit();

        // Before start time
        zeroSlope.calculateOutput(0.5);
        assertEquals("Should output initial value before start", 5.0,
            zeroSlope.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // After start time (should remain constant)
        zeroSlope.calculateOutput(5.0);
        assertEquals("Should output initial value with zero slope", 5.0,
            zeroSlope.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testDelayedStart() throws Exception {
        // Test ramp with delayed start time
        Ramp delayedRamp = Ramp.create("delayedRamp", "test", 1.5, 3.0, 2.0, mockModel);
        initializeOutputSignals(delayedRamp);
        delayedRamp.calculateInit();

        // Before start time
        delayedRamp.calculateOutput(2.0);
        assertEquals("Should output initial value before start", 2.0,
            delayedRamp.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // At start time
        delayedRamp.calculateOutput(3.0);
        assertEquals("Should output initial value at start", 2.0,
            delayedRamp.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // After start time: output = 1.5 * (5.0 - 3.0) + 2.0 = 5.0
        delayedRamp.calculateOutput(5.0);
        assertEquals("Should output ramp value after start", 5.0,
            delayedRamp.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testNegativeInitialOutput() throws Exception {
        // Test ramp with negative initial output
        Ramp negativeInitial = Ramp.create("negativeInitial", "test", 1.0, 0.0, -5.0, mockModel);
        initializeOutputSignals(negativeInitial);
        negativeInitial.calculateInit();

        // Before start time
        negativeInitial.calculateOutput(-1.0);
        assertEquals("Should output negative initial value before start", -5.0,
            negativeInitial.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // After start time: output = 1.0 * (5.0 - 0.0) + (-5.0) = 0.0
        negativeInitial.calculateOutput(5.0);
        assertEquals("Should output ramp value after start", 0.0,
            negativeInitial.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testLargeSlope() throws Exception {
        // Test ramp with very large slope
        double largeSlope = 1e6;
        Ramp largeSlopeRamp = Ramp.create("largeSlopeRamp", "test", largeSlope, 0.0, 0.0, mockModel);
        initializeOutputSignals(largeSlopeRamp);
        largeSlopeRamp.calculateInit();

        largeSlopeRamp.calculateOutput(1.0);
        assertEquals("Should handle large slope", largeSlope,
            largeSlopeRamp.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), largeSlope * 1e-10);
    }

    @Test
    public void testSmallSlope() throws Exception {
        // Test ramp with very small slope
        double smallSlope = 1e-10;
        Ramp smallSlopeRamp = Ramp.create("smallSlopeRamp", "test", smallSlope, 0.0, 0.0, mockModel);
        initializeOutputSignals(smallSlopeRamp);
        smallSlopeRamp.calculateInit();

        smallSlopeRamp.calculateOutput(1.0);
        assertEquals("Should handle small slope", smallSlope,
            smallSlopeRamp.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), 1e-20);
    }

    @Test
    public void testOutputPortConfiguration() {
        // Verify output port is properly configured
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertFalse("Ramp block should not have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testRamp", "testRamp", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be Ramp", "Ramp", block.getBlockType());
    }

    @Test
    public void testNoInputPorts() {
        assertEquals("Ramp block should have 0 input ports", 0, block.getInputPortList().size());
    }

    @Test
    public void testParameterCount() {
        // Ramp block should have parameters for slope, start, initialOutput, etc.
        assertTrue("Should have at least 6 parameters", block.getParameterList().size() >= 6);
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        block.calculateInit();

        double testTime = 5.0;

        // Call multiple times with same time value
        for (int i = 0; i < 50; i++) {
            block.calculateOutput(testTime);
            double output = getScalarOutput(0);
            assertEquals("Should always output ramp value at iteration " + i, 5.0, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        block.calculateInit();

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Ramp calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Ramp block should be very fast (simple arithmetic operation)
        assertTrue("Ramp calculateOutput should be very fast", avgTime < 5000); // <5 microseconds
    }

    @Test
    public void testRampAtVariousSlopesAndStarts() {
        // Test ramp with different configurations
        double[][] configurations = {
            {1.0, 0.0, 0.0},    // slope=1, start=0, initial=0
            {2.0, 1.0, 5.0},    // slope=2, start=1, initial=5
            {0.5, 2.0, -3.0},   // slope=0.5, start=2, initial=-3
            {-1.0, 5.0, 10.0}   // slope=-1, start=5, initial=10
        };

        for (double[] config : configurations) {
            try {
                double slope = config[0];
                double start = config[1];
                double initialOutput = config[2];

                Ramp testRamp = Ramp.create("test", "test", slope, start, initialOutput, mockModel);
                initializeOutputSignals(testRamp);
                testRamp.calculateInit();

                // Before start
                testRamp.calculateOutput(start - 0.5);
                assertEquals("Should output initial value before start", initialOutput,
                    testRamp.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

                // After start
                double testTime = start + 2.0;
                testRamp.calculateOutput(testTime);
                double expectedOutput = slope * (testTime - start) + initialOutput;
                assertEquals("Should output ramp value after start", expectedOutput,
                    testRamp.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

            } catch (Exception e) {
                fail("Failed to test ramp configuration: " + e.getMessage());
            }
        }
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        block.calculateInit();
        block.calculateOutput(2.0);
        double firstOutput = getScalarOutput(0);

        block.calculateOutput(5.0);
        double secondOutput = getScalarOutput(0);

        // Reinitialize
        block.calculateInit();
        block.calculateOutput(2.0);
        double reinitOutput = getScalarOutput(0);

        assertEquals("Output should be same after reinitialization", firstOutput, reinitOutput, DELTA);
        assertNotEquals("Outputs at different times should differ", firstOutput, secondOutput, DELTA);
    }

    @Test
    public void testSequentialBlocks() throws Exception {
        // Test creating multiple ramp blocks with different configurations
        Ramp ramp1 = Ramp.create("ramp1", "test", 1.0, 0.0, 0.0, mockModel);
        Ramp ramp2 = Ramp.create("ramp2", "test", 2.0, 1.0, 5.0, mockModel);
        Ramp ramp3 = Ramp.create("ramp3", "test", 0.5, 2.0, -3.0, mockModel);

        initializeOutputSignals(ramp1);
        initializeOutputSignals(ramp2);
        initializeOutputSignals(ramp3);

        ramp1.calculateInit();
        ramp2.calculateInit();
        ramp3.calculateInit();

        double testTime = 5.0;
        ramp1.calculateOutput(testTime);
        ramp2.calculateOutput(testTime);
        ramp3.calculateOutput(testTime);

        // ramp1: output = 1.0 * (5.0 - 0.0) + 0.0 = 5.0
        assertEquals("Ramp1 should output correct value", 5.0,
            ramp1.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // ramp2: output = 2.0 * (5.0 - 1.0) + 5.0 = 13.0
        assertEquals("Ramp2 should output correct value", 13.0,
            ramp2.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // ramp3: output = 0.5 * (5.0 - 2.0) + (-3.0) = -1.5
        assertEquals("Ramp3 should output correct value", -1.5,
            ramp3.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testNoDriftOverTime() {
        // Verify that output doesn't drift over many iterations
        block.calculateInit();

        double testTime = 3.0;
        double expectedOutput = 3.0; // slope=1.0, start=0.0, initialOutput=0.0

        // Test consistency over many iterations
        for (int i = 0; i < 1000; i++) {
            block.calculateOutput(testTime);
            double output = getScalarOutput(0);
            assertEquals("Ramp output should not drift at iteration " + i, expectedOutput, output, DELTA);
        }
    }

    @Test
    public void testRampContinuity() throws Exception {
        // Verify ramp output is continuous at start time
        Ramp continuousRamp = Ramp.create("continuousRamp", "test", 1.0, 5.0, 10.0, mockModel);
        initializeOutputSignals(continuousRamp);
        continuousRamp.calculateInit();

        // Just before start time
        continuousRamp.calculateOutput(4.999);
        double beforeStart = continuousRamp.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();

        // At start time
        continuousRamp.calculateOutput(5.0);
        double atStart = continuousRamp.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();

        // Just after start time
        continuousRamp.calculateOutput(5.001);
        double afterStart = continuousRamp.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();

        // Before and at start should be initial output
        assertEquals("Before start should be initial output", 10.0, beforeStart, DELTA);
        assertEquals("At start should be initial output", 10.0, atStart, DELTA);

        // After start should be very close to initial output (small increment)
        assertTrue("After start should be close to initial output", Math.abs(afterStart - 10.0) < 0.002);
    }

    @Test
    public void testRampInitialization() {
        // Test that calculateInit() properly initializes the ramp
        block.calculateInit();
        assertEquals("Ramp should initialize to initial output", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testZeroStartTime() throws Exception {
        // Test ramp with start time at t=0
        Ramp zeroStart = Ramp.create("zeroStart", "test", 2.0, 0.0, 5.0, mockModel);
        initializeOutputSignals(zeroStart);
        zeroStart.calculateInit();

        zeroStart.calculateOutput(0.0);
        assertEquals("Should output initial value at t=0", 5.0,
            zeroStart.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        zeroStart.calculateOutput(3.0);
        assertEquals("Should output ramp value after start", 11.0,
            zeroStart.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testHighPrecisionSlope() throws Exception {
        // Test ramp with high-precision slope
        double preciseSlope = 1.23456789;
        Ramp preciseRamp = Ramp.create("preciseRamp", "test", preciseSlope, 0.0, 0.0, mockModel);
        initializeOutputSignals(preciseRamp);
        preciseRamp.calculateInit();

        preciseRamp.calculateOutput(1.0);
        assertEquals("Should maintain high-precision slope", preciseSlope,
            preciseRamp.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), 1e-14);
    }
}
