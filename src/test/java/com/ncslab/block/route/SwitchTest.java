package com.ncslab.block.route;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.OutputSignal;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Switch block.
 * Tests calculateOutput() directly without WebSocket dependencies.
 *
 * Switch block has 3 inputs:
 * - Port 0: First data input (selected when condition is true)
 * - Port 1: Control signal (compared against threshold)
 * - Port 2: Second data input (selected when condition is false)
 */
public class SwitchTest extends DirectBlockTestBase {

    private Switch switchBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create Switch block with threshold=0.0, criteria=">="
        switchBlock = Switch.create("testSwitch", "test", 0.0, ">=", mockModel);
        return switchBlock;
    }

    @Test
    public void testConditionTrue() {
        // Test: Control signal >= threshold should select input 1
        setScalarInput(0, 10.0);  // First input
        setScalarInput(1, 5.0);   // Control signal (>= 0.0 is true)
        setScalarInput(2, 20.0);  // Second input

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Control >= threshold should select first input", 10.0, output, DELTA);
    }

    @Test
    public void testConditionFalse() {
        // Test: Control signal < threshold should select input 2
        setScalarInput(0, 10.0);  // First input
        setScalarInput(1, -5.0);  // Control signal (< 0.0 is false)
        setScalarInput(2, 20.0);  // Second input

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Control < threshold should select second input", 20.0, output, DELTA);
    }

    @Test
    public void testExactThreshold() {
        // Test: Control signal exactly at threshold with ">=" should select input 1
        setScalarInput(0, 10.0);  // First input
        setScalarInput(1, 0.0);   // Control signal (== 0.0, with >= this is true)
        setScalarInput(2, 20.0);  // Second input

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Control == threshold with >= should select first input", 10.0, output, DELTA);
    }

    @Test
    public void testStrictGreaterThan() throws Exception {
        // Create Switch with ">" criteria
        Switch gtSwitch = Switch.create("gtSwitch", "test", 0.0, ">", mockModel);
        gtSwitch.calculateInit();

        // Exact threshold should select input 2 with ">"
        mockInputPortConnection(gtSwitch, 0, new Data(10.0));
        mockInputPortConnection(gtSwitch, 1, new Data(0.0));
        mockInputPortConnection(gtSwitch, 2, new Data(20.0));

        gtSwitch.calculateOutput(0.0);

        OutputSignal outputSignal = gtSwitch.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        double output = outputData.getInitValue();
        assertEquals("Control == threshold with > should select second input", 20.0, output, DELTA);

        // Slightly above threshold should select input 1
        mockInputPortConnection(gtSwitch, 1, new Data(0.1));
        gtSwitch.calculateOutput(0.0);

        outputSignal = gtSwitch.getOutputPortList().get(0).getOutputSignalC();
        outputData = outputSignal.getData();
        output = outputData.getInitValue();
        assertEquals("Control > threshold should select first input", 10.0, output, DELTA);
    }

    @Test
    public void testNotEqualCriteria() throws Exception {
        // Create Switch with "~=" (not equal) criteria
        Switch neSwitch = Switch.create("neSwitch", "test", 0.0, "~=", mockModel);
        neSwitch.calculateInit();

        // Control != threshold should select input 1
        mockInputPortConnection(neSwitch, 0, new Data(10.0));
        mockInputPortConnection(neSwitch, 1, new Data(5.0));
        mockInputPortConnection(neSwitch, 2, new Data(20.0));

        neSwitch.calculateOutput(0.0);

        OutputSignal outputSignal = neSwitch.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        double output = outputData.getInitValue();
        assertEquals("Control != threshold should select first input", 10.0, output, DELTA);

        // Control == threshold should select input 2
        mockInputPortConnection(neSwitch, 1, new Data(0.0));
        neSwitch.calculateOutput(0.0);

        outputSignal = neSwitch.getOutputPortList().get(0).getOutputSignalC();
        outputData = outputSignal.getData();
        output = outputData.getInitValue();
        assertEquals("Control == threshold with ~= should select second input", 20.0, output, DELTA);
    }

    @Test
    public void testNegativeThreshold() throws Exception {
        // Create Switch with negative threshold
        Switch negSwitch = Switch.create("negSwitch", "test", -5.0, ">=", mockModel);
        negSwitch.calculateInit();

        // Control < negative threshold should select input 2
        mockInputPortConnection(negSwitch, 0, new Data(10.0));
        mockInputPortConnection(negSwitch, 1, new Data(-10.0));
        mockInputPortConnection(negSwitch, 2, new Data(20.0));

        negSwitch.calculateOutput(0.0);

        OutputSignal outputSignal = negSwitch.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        double output = outputData.getInitValue();
        assertEquals("Control < negative threshold should select second input", 20.0, output, DELTA);

        // Control >= negative threshold should select input 1
        mockInputPortConnection(negSwitch, 1, new Data(-3.0));
        negSwitch.calculateOutput(0.0);

        outputSignal = negSwitch.getOutputPortList().get(0).getOutputSignalC();
        outputData = outputSignal.getData();
        output = outputData.getInitValue();
        assertEquals("Control >= negative threshold should select first input", 10.0, output, DELTA);
    }

    @Test
    public void testZeroInputs() {
        // Test: All zero inputs
        setScalarInput(0, 0.0);
        setScalarInput(1, 0.0);
        setScalarInput(2, 0.0);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("All zero inputs should output zero", 0.0, output, DELTA);
    }

    @Test
    public void testLargeValues() {
        // Test: Large input values
        setScalarInput(0, 1e10);
        setScalarInput(1, 1e5);
        setScalarInput(2, 2e10);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Large control value should select first input", 1e10, output, 1e-4);
    }

    @Test
    public void testMultipleSwitching() {
        // Test: Multiple switching scenarios
        double[][] testCases = {
            // {input1, control, input2, expectedOutput}
            {10.0, 5.0, 20.0, 10.0},    // control >= 0, select input1
            {10.0, -5.0, 20.0, 20.0},   // control < 0, select input2
            {15.0, 0.0, 25.0, 15.0},    // control == 0 (threshold), select input1
            {-5.0, 10.0, -10.0, -5.0},  // positive control, select input1
            {100.0, -0.1, 200.0, 200.0} // slightly negative control, select input2
        };

        for (double[] testCase : testCases) {
            setScalarInput(0, testCase[0]);
            setScalarInput(1, testCase[1]);
            setScalarInput(2, testCase[2]);

            block.calculateOutput(0.0);

            double output = getScalarOutput(0);
            assertEquals(String.format("Switch with control=%.1f should output %.1f",
                testCase[1], testCase[3]), testCase[3], output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 10.0);
        setScalarInput(1, 5.0);
        setScalarInput(2, 20.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Switch calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 15 microseconds on average
        assertTrue("Switch calculateOutput should be fast", avgTime < 15000);
    }

    @Test
    public void testBoundaryConditions() throws Exception {
        // Test: Threshold at various boundary values
        double[] thresholds = {Double.MIN_VALUE, -1e-10, 0.0, 1e-10, Double.MAX_VALUE};

        for (double threshold : thresholds) {
            Switch boundarySwitch = Switch.create("boundarySwitch", "test", threshold, ">=", mockModel);
            boundarySwitch.calculateInit();

            mockInputPortConnection(boundarySwitch, 0, new Data(10.0));
            mockInputPortConnection(boundarySwitch, 1, new Data(threshold));
            mockInputPortConnection(boundarySwitch, 2, new Data(20.0));

            boundarySwitch.calculateOutput(0.0);

            OutputSignal outputSignal = boundarySwitch.getOutputPortList().get(0).getOutputSignalC();
            Data outputData = outputSignal.getData();
            double output = outputData.getInitValue();
            assertEquals("Control at threshold " + threshold + " should select first input", 10.0, output, DELTA);
        }
    }
}
