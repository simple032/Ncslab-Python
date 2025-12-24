package com.ncslab.block.source;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Clock block.
 * Tests calculateInit() and output generation without WebSocket dependencies.
 *
 * Test Coverage:
 * - Time output at various simulation times
 * - Initial value at t=0
 * - Clock output matches simulation time
 * - Edge cases (zero time, negative time, large time)
 * - Performance benchmarks
 */
public class ClockTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create Clock block: sampleTime=0.0, outDataType="double", saturateOnOverflow=false
        return Clock.create("testClock", "test", 0.0, "double", false, mockModel);
    }

    @Test
    public void testInitialTime() {
        // Test output at t=0
        block.calculateOutput(0.0);
        assertEquals("Should output 0.0 at t=0", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testTimeOutput() {
        // Test output matches simulation time
        double[] testTimes = {0.0, 0.5, 1.0, 2.5, 5.0, 10.0, 100.0};

        for (double time : testTimes) {
            block.calculateOutput(time);
            assertEquals("Clock output should match simulation time at t=" + time,
                time, getScalarOutput(0), DELTA);
        }
    }

    @Test
    public void testMultipleTimePoints() {
        // Verify clock behavior across multiple time points
        double[][] testCases = {
            {0.0, 0.0},     // t=0.0 → 0.0
            {0.1, 0.1},     // t=0.1 → 0.1
            {0.5, 0.5},     // t=0.5 → 0.5
            {1.0, 1.0},     // t=1.0 → 1.0
            {2.5, 2.5},     // t=2.5 → 2.5
            {10.0, 10.0},   // t=10.0 → 10.0
            {100.0, 100.0}  // t=100.0 → 100.0
        };

        for (double[] testCase : testCases) {
            double time = testCase[0];
            double expected = testCase[1];
            block.calculateOutput(time);
            assertEquals("Clock output mismatch at t=" + time, expected, getScalarOutput(0), DELTA);
        }
    }

    @Test
    public void testLargeTimeValue() throws Exception {
        // Test clock at very large time value
        double largeTime = 1e6;
        Clock largeClock = Clock.create("largeClock", "test", 0.0, "double", false, mockModel);
        initializeOutputSignals(largeClock);
        largeClock.calculateInit();

        largeClock.calculateOutput(largeTime);
        assertEquals("Clock should output large time value", largeTime,
            largeClock.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), largeTime * 1e-10);
    }

    @Test
    public void testSmallTimeValue() throws Exception {
        // Test clock at very small time value
        double smallTime = 1e-10;
        Clock smallClock = Clock.create("smallClock", "test", 0.0, "double", false, mockModel);
        initializeOutputSignals(smallClock);
        smallClock.calculateInit();

        smallClock.calculateOutput(smallTime);
        assertEquals("Clock should output small time value", smallTime,
            smallClock.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), 1e-20);
    }

    @Test
    public void testZeroTime() {
        // Test clock at t=0 (should output 0.0)
        block.calculateOutput(0.0);
        assertEquals("Clock should output 0.0 at t=0", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSequentialTimeCalls() {
        // Test multiple sequential time calls
        double[] times = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5};

        for (double time : times) {
            block.calculateOutput(time);
            assertEquals("Clock should output current time at t=" + time,
                time, getScalarOutput(0), DELTA);
        }
    }

    @Test
    public void testNonSequentialTimeCalls() {
        // Test non-sequential time calls (time can go backwards or forwards)
        double[] times = {5.0, 2.0, 10.0, 1.0, 8.0};

        for (double time : times) {
            block.calculateOutput(time);
            assertEquals("Clock should output current time at t=" + time,
                time, getScalarOutput(0), DELTA);
        }
    }

    @Test
    public void testOutputPortConfiguration() {
        // Verify output port is properly configured
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertFalse("Clock block should not have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testClock", "testClock", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be Clock", "Clock", block.getBlockType());
    }

    @Test
    public void testNoInputPorts() {
        assertEquals("Clock block should have 0 input ports", 0, block.getInputPortList().size());
    }

    @Test
    public void testParameterCount() {
        // Clock block should have parameters for sample time, data type, etc.
        assertTrue("Should have at least 3 parameters", block.getParameterList().size() >= 3);
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
            assertEquals("Clock output should be consistent at iteration " + i, testTime, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        block.calculateInit();

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Clock calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Clock block should be very fast (simple time assignment)
        assertTrue("Clock calculateOutput should be very fast", avgTime < 2000); // <2 microseconds
    }

    @Test
    public void testVaryingSampleTimes() throws Exception {
        // Test clocks with different sample times
        double[] sampleTimes = {0.0, 0.01, 0.1, 1.0, -1.0};

        for (double sampleTime : sampleTimes) {
            Clock testClock = Clock.create("clock_" + sampleTime, "test", sampleTime, "double", false, mockModel);
            initializeOutputSignals(testClock);
            testClock.calculateInit();

            double testTime = 5.0;
            testClock.calculateOutput(testTime);
            assertEquals("Clock with sampleTime=" + sampleTime + " should output time", testTime,
                testClock.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
        }
    }

    @Test
    public void testDifferentDataTypes() throws Exception {
        // Test clock with different output data types
        String[] dataTypes = {"double", "single", "int32", "uint32"};

        for (String dataType : dataTypes) {
            Clock testClock = Clock.create("clock_" + dataType, "test", 0.0, dataType, false, mockModel);
            initializeOutputSignals(testClock);
            testClock.calculateInit();

            double testTime = 3.5;
            testClock.calculateOutput(testTime);

            // All data types should support time output
            assertNotNull("Clock with dataType=" + dataType + " should produce output",
                testClock.getOutputPortList().get(0).getOutputSignalC().getData());
        }
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        block.calculateInit();
        block.calculateOutput(5.0);
        double firstOutput = getScalarOutput(0);

        block.calculateOutput(10.0);
        double secondOutput = getScalarOutput(0);

        // Reinitialize
        block.calculateInit();
        block.calculateOutput(5.0);
        double reinitOutput = getScalarOutput(0);

        assertEquals("Output should be same after reinitialization", firstOutput, reinitOutput, DELTA);
        assertNotEquals("Outputs at different times should differ", firstOutput, secondOutput, DELTA);
    }

    @Test
    public void testSequentialBlocks() throws Exception {
        // Test creating multiple clock blocks with different configurations
        Clock clock1 = Clock.create("clock1", "test", 0.0, "double", false, mockModel);
        Clock clock2 = Clock.create("clock2", "test", 0.1, "single", false, mockModel);
        Clock clock3 = Clock.create("clock3", "test", -1.0, "int32", false, mockModel);

        initializeOutputSignals(clock1);
        initializeOutputSignals(clock2);
        initializeOutputSignals(clock3);

        clock1.calculateInit();
        clock2.calculateInit();
        clock3.calculateInit();

        double testTime = 7.5;
        clock1.calculateOutput(testTime);
        clock2.calculateOutput(testTime);
        clock3.calculateOutput(testTime);

        // All clocks should output the same time value
        assertEquals("Clock1 should output test time", testTime,
            clock1.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
        assertEquals("Clock2 should output test time", testTime,
            clock2.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
        assertEquals("Clock3 should output test time", testTime,
            clock3.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testNoDriftOverTime() {
        // Verify that output doesn't drift over many iterations
        block.calculateInit();

        double testTime = 2.5;

        // Test consistency over many iterations
        for (int i = 0; i < 1000; i++) {
            block.calculateOutput(testTime);
            double output = getScalarOutput(0);
            assertEquals("Clock output should not drift at iteration " + i, testTime, output, DELTA);
        }
    }

    @Test
    public void testClockAtVaryingFrequencies() {
        // Test clock output at varying time frequencies
        double[] timeSequence = {
            0.0, 0.001, 0.002, 0.01, 0.1, 1.0, 10.0, 100.0
        };

        for (double time : timeSequence) {
            block.calculateOutput(time);
            assertEquals("Clock should accurately track time at t=" + time,
                time, getScalarOutput(0), DELTA);
        }
    }

    @Test
    public void testHighPrecisionTime() {
        // Test clock with high-precision time values
        double highPrecisionTime = 1.23456789012345;
        block.calculateOutput(highPrecisionTime);
        assertEquals("Clock should maintain high-precision time",
            highPrecisionTime, getScalarOutput(0), 1e-14);
    }

    @Test
    public void testClockInitialization() {
        // Test that calculateInit() properly initializes the clock
        block.calculateInit();
        assertEquals("Clock should initialize to 0.0", 0.0, getScalarOutput(0), DELTA);
    }
}
