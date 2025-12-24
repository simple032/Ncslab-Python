package com.ncslab.block.source;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Step block.
 * Tests calculateInit() and output generation without WebSocket dependencies.
 *
 * Test Coverage:
 * - Step transition at specified time
 * - Initial value output before step time
 * - Final value output at and after step time
 * - Edge cases (zero time, negative values, large values)
 * - Performance benchmarks
 */
public class StepTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create Step block: stepTime=1.0, initialValue=0.0, finalValue=1.0
        return Step.create("testStep", "test", 1.0, 0.0, 1.0, mockModel);
    }

    @Test
    public void testBeforeStepTime() {
        // Test output before step occurs (t < stepTime)
        block.calculateOutput(0.5);
        assertEquals("Should output initial value before step", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testAtStepTime() {
        // Test output exactly at step time
        block.calculateOutput(1.0);
        assertEquals("Should output final value at step time", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testAfterStepTime() {
        // Test output after step occurs (t > stepTime)
        block.calculateOutput(2.0);
        assertEquals("Should output final value after step", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testMultipleTimePoints() {
        // Verify step behavior across multiple time points
        double[][] testCases = {
            {0.0, 0.0},   // t=0.0 → initialValue
            {0.5, 0.0},   // t=0.5 → initialValue
            {0.99, 0.0},  // t=0.99 → initialValue
            {1.0, 1.0},   // t=1.0 → finalValue
            {1.01, 1.0},  // t=1.01 → finalValue
            {10.0, 1.0}   // t=10.0 → finalValue
        };

        for (double[] testCase : testCases) {
            double time = testCase[0];
            double expected = testCase[1];
            block.calculateOutput(time);
            assertEquals("Step output mismatch at t=" + time, expected, getScalarOutput(0), DELTA);
        }
    }

    @Test
    public void testNegativeToPositiveStep() throws Exception {
        // Test step from negative to positive value
        Step negativeStep = Step.create("negStep", "test", 2.0, -5.0, 5.0, mockModel);
        initializeOutputSignals(negativeStep);
        negativeStep.calculateInit();

        negativeStep.calculateOutput(1.0);
        assertEquals("Should output initial value before step", -5.0,
            negativeStep.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        negativeStep.calculateOutput(2.0);
        assertEquals("Should output final value at step", 5.0,
            negativeStep.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        negativeStep.calculateOutput(3.0);
        assertEquals("Should output final value after step", 5.0,
            negativeStep.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testZeroStepTime() throws Exception {
        // Test step occurring at t=0
        Step zeroTimeStep = Step.create("zeroStep", "test", 0.0, 0.0, 10.0, mockModel);
        initializeOutputSignals(zeroTimeStep);
        zeroTimeStep.calculateInit();

        zeroTimeStep.calculateOutput(0.0);
        assertEquals("Should step immediately at t=0", 10.0,
            zeroTimeStep.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testLargeStepTime() throws Exception {
        // Test step at large time value
        Step largeTimeStep = Step.create("largeStep", "test", 1000.0, 1.0, 2.0, mockModel);
        initializeOutputSignals(largeTimeStep);
        largeTimeStep.calculateInit();

        largeTimeStep.calculateOutput(999.9);
        assertEquals("Should output initial value before large step time", 1.0,
            largeTimeStep.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        largeTimeStep.calculateOutput(1000.0);
        assertEquals("Should output final value at large step time", 2.0,
            largeTimeStep.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testZeroToZeroStep() throws Exception {
        // Test step from 0 to 0 (edge case)
        Step zeroToZero = Step.create("zeroToZero", "test", 1.0, 0.0, 0.0, mockModel);
        initializeOutputSignals(zeroToZero);
        zeroToZero.calculateInit();

        zeroToZero.calculateOutput(0.5);
        assertEquals("Should output 0.0 before step", 0.0,
            zeroToZero.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        zeroToZero.calculateOutput(1.5);
        assertEquals("Should output 0.0 after step", 0.0,
            zeroToZero.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testLargeStepValues() throws Exception {
        // Test step with very large values
        double largeValue = 1e10;
        Step largeValueStep = Step.create("largeValue", "test", 1.0, -largeValue, largeValue, mockModel);
        initializeOutputSignals(largeValueStep);
        largeValueStep.calculateInit();

        largeValueStep.calculateOutput(0.5);
        assertEquals("Should output large negative initial value", -largeValue,
            largeValueStep.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), largeValue * 1e-10);

        largeValueStep.calculateOutput(1.5);
        assertEquals("Should output large positive final value", largeValue,
            largeValueStep.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), largeValue * 1e-10);
    }

    @Test
    public void testSmallStepValues() throws Exception {
        // Test step with very small values
        double smallValue = 1e-10;
        Step smallValueStep = Step.create("smallValue", "test", 1.0, 0.0, smallValue, mockModel);
        initializeOutputSignals(smallValueStep);
        smallValueStep.calculateInit();

        smallValueStep.calculateOutput(0.5);
        assertEquals("Should output 0.0 before step", 0.0,
            smallValueStep.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        smallValueStep.calculateOutput(1.5);
        assertEquals("Should output small value after step", smallValue,
            smallValueStep.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), 1e-20);
    }

    @Test
    public void testOutputPortConfiguration() {
        // Verify output port is properly configured
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertFalse("Step block should not have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testStep", "testStep", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be Step", "Step", block.getBlockType());
    }

    @Test
    public void testNoInputPorts() {
        assertEquals("Step block should have 0 input ports", 0, block.getInputPortList().size());
    }

    @Test
    public void testParameterCount() {
        // Step block should have parameters for step time, initial value, final value, etc.
        assertTrue("Should have at least 3 parameters", block.getParameterList().size() >= 3);
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        block.calculateInit();

        // Before step time
        for (int i = 0; i < 50; i++) {
            block.calculateOutput(0.5);
            double output = getScalarOutput(0);
            assertEquals("Should always output initial value before step", 0.0, output, DELTA);
        }

        // After step time
        for (int i = 0; i < 50; i++) {
            block.calculateOutput(2.0);
            double output = getScalarOutput(0);
            assertEquals("Should always output final value after step", 1.0, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        block.calculateInit();

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Step calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Step block should be very fast (simple comparison operation)
        assertTrue("Step calculateOutput should be very fast", avgTime < 5000); // <5 microseconds
    }

    @Test
    public void testStepAtVariousTimes() {
        // Test step occurring at different time values
        double[][] configurations = {
            {0.1, 0.0, 1.0},   // Step at t=0.1
            {0.5, 2.0, 3.0},   // Step at t=0.5
            {5.0, -1.0, 1.0},  // Step at t=5.0
            {10.0, 0.0, 100.0} // Step at t=10.0
        };

        for (double[] config : configurations) {
            try {
                double stepTime = config[0];
                double initialValue = config[1];
                double finalValue = config[2];

                Step testStep = Step.create("test", "test", stepTime, initialValue, finalValue, mockModel);
                initializeOutputSignals(testStep);
                testStep.calculateInit();

                // Before step
                testStep.calculateOutput(stepTime * 0.9);
                assertEquals("Should output initial value before step at t=" + stepTime, initialValue,
                    testStep.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

                // At step
                testStep.calculateOutput(stepTime);
                assertEquals("Should output final value at step time t=" + stepTime, finalValue,
                    testStep.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

                // After step
                testStep.calculateOutput(stepTime * 1.1);
                assertEquals("Should output final value after step at t=" + stepTime, finalValue,
                    testStep.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

            } catch (Exception e) {
                fail("Failed to test step configuration: " + e.getMessage());
            }
        }
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        block.calculateInit();
        block.calculateOutput(0.5);
        double beforeStepOutput = getScalarOutput(0);

        block.calculateOutput(2.0);
        double afterStepOutput = getScalarOutput(0);

        // Reinitialize
        block.calculateInit();
        block.calculateOutput(0.5);
        double reinitOutput = getScalarOutput(0);

        assertEquals("Output should be same after reinitialization", beforeStepOutput, reinitOutput, DELTA);
        assertNotEquals("Before and after step should differ", beforeStepOutput, afterStepOutput, DELTA);
    }

    @Test
    public void testSequentialBlocks() throws Exception {
        // Test creating multiple step blocks with different configurations
        Step step1 = Step.create("step1", "test", 1.0, 0.0, 1.0, mockModel);
        Step step2 = Step.create("step2", "test", 2.0, 5.0, 10.0, mockModel);
        Step step3 = Step.create("step3", "test", 0.5, -1.0, 1.0, mockModel);

        initializeOutputSignals(step1);
        initializeOutputSignals(step2);
        initializeOutputSignals(step3);

        step1.calculateInit();
        step2.calculateInit();
        step3.calculateInit();

        double testTime = 1.5;
        step1.calculateOutput(testTime);
        step2.calculateOutput(testTime);
        step3.calculateOutput(testTime);

        assertEquals("Step1 should output final value", 1.0,
            step1.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
        assertEquals("Step2 should output initial value", 5.0,
            step2.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
        assertEquals("Step3 should output final value", 1.0,
            step3.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testNoDriftOverTime() {
        // Verify that output doesn't drift over many iterations
        block.calculateInit();

        // Test before step time - should always output initial value
        for (int i = 0; i < 1000; i++) {
            block.calculateOutput(0.5);
            double output = getScalarOutput(0);
            assertEquals("Initial value should not drift at iteration " + i, 0.0, output, DELTA);
        }

        // Test after step time - should always output final value
        for (int i = 0; i < 1000; i++) {
            block.calculateOutput(2.0);
            double output = getScalarOutput(0);
            assertEquals("Final value should not drift at iteration " + i, 1.0, output, DELTA);
        }
    }
}
