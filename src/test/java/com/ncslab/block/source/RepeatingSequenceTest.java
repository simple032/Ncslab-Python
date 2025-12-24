package com.ncslab.block.source;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for RepeatingSequence block.
 * Tests calculateInit() and output generation without WebSocket dependencies.
 *
 * Test Coverage:
 * - Sequence repetition at various times
 * - Time vector and output value alignment
 * - Interpolation between time points
 * - Edge cases (boundary conditions, single period, multiple periods)
 * - Performance benchmarks
 */
public class RepeatingSequenceTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create RepeatingSequence block: timeValues="[0 1 2]", outputValues="[0 1 0]"
        return RepeatingSequence.create("testSequence", "test", "[0 1 2]", "[0 1 0]", mockModel);
    }

    @Test
    public void testInitialOutput() {
        // Test output at t=0 (should be first value in sequence = 0)
        assertEquals("Should output first value at t=0", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSequenceAtTimePoints() {
        // Test output at exact time points in the sequence
        // Time: [0 1 2], Output: [0 1 0]

        block.calculateOutput(0.0);
        assertEquals("Should output 0.0 at t=0", 0.0, getScalarOutput(0), DELTA);

        block.calculateOutput(1.0);
        assertEquals("Should output 1.0 at t=1", 1.0, getScalarOutput(0), DELTA);

        block.calculateOutput(2.0);
        assertEquals("Should output 0.0 at t=2", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testInterpolation() {
        // Test linear interpolation between time points
        // Between t=0 and t=1: output goes from 0 to 1 linearly

        block.calculateOutput(0.5);
        assertEquals("Should interpolate to 0.5 at t=0.5", 0.5, getScalarOutput(0), DELTA);

        // Between t=1 and t=2: output goes from 1 to 0 linearly
        block.calculateOutput(1.5);
        assertEquals("Should interpolate to 0.5 at t=1.5", 0.5, getScalarOutput(0), DELTA);

        block.calculateOutput(0.25);
        assertEquals("Should interpolate to 0.25 at t=0.25", 0.25, getScalarOutput(0), DELTA);

        block.calculateOutput(1.75);
        assertEquals("Should interpolate to 0.25 at t=1.75", 0.25, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSequenceRepetition() {
        // Test that sequence repeats after period (t=2)

        // First period
        block.calculateOutput(0.0);
        double output1 = getScalarOutput(0);

        block.calculateOutput(1.0);
        double output2 = getScalarOutput(0);

        // Second period (should repeat)
        block.calculateOutput(2.0);
        double output3 = getScalarOutput(0);

        block.calculateOutput(3.0);
        double output4 = getScalarOutput(0);

        assertEquals("Sequence should repeat: t=0 and t=2", output1, output3, DELTA);
        assertEquals("Sequence should repeat: t=1 and t=3", output2, output4, DELTA);
    }

    @Test
    public void testMultiplePeriods() {
        // Test sequence over multiple periods
        double[][] testCases = {
            {0.0, 0.0},   // First period
            {1.0, 1.0},
            {2.0, 0.0},   // Second period starts
            {3.0, 1.0},
            {4.0, 0.0},   // Third period starts
            {5.0, 1.0},
            {6.0, 0.0},   // Fourth period starts
            {10.0, 0.0},  // Sixth period starts
        };

        for (double[] testCase : testCases) {
            double time = testCase[0];
            double expected = testCase[1];
            block.calculateOutput(time);
            assertEquals("Sequence output mismatch at t=" + time, expected, getScalarOutput(0), DELTA);
        }
    }

    @Test
    public void testDifferentSequence() throws Exception {
        // Test with different time and output values: [0 0.5 1], [1 2 3]
        // Note: sequence repeats at t=1.0, so t=1.0 wraps to t=0 (output=1)
        RepeatingSequence diffSeq = RepeatingSequence.create("diffSeq", "test", "[0 0.5 1]", "[1 2 3]", mockModel);
        initializeOutputSignals(diffSeq);
        diffSeq.calculateInit();

        // At t=0: output = 1
        diffSeq.calculateOutput(0.0);
        assertEquals("Should output 1.0 at t=0", 1.0,
            diffSeq.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // At t=0.5: output = 2 (exact match on time point)
        diffSeq.calculateOutput(0.5);
        assertEquals("Should output 2.0 at t=0.5", 2.0,
            diffSeq.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // At t=0.99: close to end, interpolate between 2 and 3
        diffSeq.calculateOutput(0.99);
        double expected = 2.0 + (0.99 - 0.5) / (1.0 - 0.5) * (3.0 - 2.0);
        assertEquals("Should interpolate near end", expected,
            diffSeq.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // At t=0.25: interpolate between 1 and 2 → 1 + 0.25/0.5 * (2-1) = 1.5
        diffSeq.calculateOutput(0.25);
        assertEquals("Should interpolate to 1.5 at t=0.25", 1.5,
            diffSeq.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testSquareWaveSequence() throws Exception {
        // Test square wave pattern: [0 1 1 2], [0 0 1 1]
        RepeatingSequence squareWave = RepeatingSequence.create("squareWave", "test", "[0 1 1 2]", "[0 0 1 1]", mockModel);
        initializeOutputSignals(squareWave);
        squareWave.calculateInit();

        squareWave.calculateOutput(0.0);
        assertEquals("Should output 0.0 at t=0", 0.0,
            squareWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        squareWave.calculateOutput(0.5);
        assertEquals("Should output 0.0 at t=0.5 (before step)", 0.0,
            squareWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        squareWave.calculateOutput(1.0);
        // At t=1.0, there are two values (both 0 and 1 defined), interpolation from 0 to 1 gives 0
        // Actually this tests the behavior at duplicate time points
        double output = squareWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        assertTrue("Should handle duplicate time points", output >= 0.0 && output <= 1.0);
    }

    @Test
    public void testSawtoothWaveSequence() throws Exception {
        // Test sawtooth pattern: [0 1 2 3], [0 1 2 0]
        RepeatingSequence sawtoothWave = RepeatingSequence.create("sawtooth", "test", "[0 1 2 3]", "[0 1 2 0]", mockModel);
        initializeOutputSignals(sawtoothWave);
        sawtoothWave.calculateInit();

        sawtoothWave.calculateOutput(0.0);
        assertEquals("Should output 0.0 at t=0", 0.0,
            sawtoothWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        sawtoothWave.calculateOutput(1.0);
        assertEquals("Should output 1.0 at t=1", 1.0,
            sawtoothWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        sawtoothWave.calculateOutput(2.0);
        assertEquals("Should output 2.0 at t=2", 2.0,
            sawtoothWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // At t=2.5: interpolate between 2 and 0 (at t=3) → 2 + 0.5 * (0 - 2) = 1
        sawtoothWave.calculateOutput(2.5);
        assertEquals("Should interpolate to 1.0 at t=2.5", 1.0,
            sawtoothWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testNegativeValues() throws Exception {
        // Test sequence with negative values: [0 1 2], [-1 0 1]
        // Period is 2, so t=2 wraps to t=0 (output=-1)
        RepeatingSequence negSeq = RepeatingSequence.create("negSeq", "test", "[0 1 2]", "[-1 0 1]", mockModel);
        initializeOutputSignals(negSeq);
        negSeq.calculateInit();

        negSeq.calculateOutput(0.0);
        assertEquals("Should output -1.0 at t=0", -1.0,
            negSeq.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        negSeq.calculateOutput(1.0);
        assertEquals("Should output 0.0 at t=1", 0.0,
            negSeq.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // At t=1.5: interpolate between 0 and 1 → 0 + 0.5 * (1 - 0) = 0.5
        negSeq.calculateOutput(1.5);
        assertEquals("Should interpolate to 0.5 at t=1.5", 0.5,
            negSeq.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testSequenceLargeValues() throws Exception {
        // Test sequence with large values
        // Note: t=1.0 wraps to t=0, so test at t=0.5 instead
        RepeatingSequence largeSeq = RepeatingSequence.create("largeSeq", "test", "[0 1]", "[0 1000000]", mockModel);
        initializeOutputSignals(largeSeq);
        largeSeq.calculateInit();

        largeSeq.calculateOutput(0.5);
        assertEquals("Should handle large values", 500000.0,  // Interpolated: 0 + 0.5 * 1000000
            largeSeq.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testSequenceSmallValues() throws Exception {
        // Test sequence with very small values
        // Note: t=1.0 wraps to t=0, so test at t=0.5 instead
        RepeatingSequence smallSeq = RepeatingSequence.create("smallSeq", "test", "[0 1]", "[0 0.000001]", mockModel);
        initializeOutputSignals(smallSeq);
        smallSeq.calculateInit();

        smallSeq.calculateOutput(0.5);
        assertEquals("Should handle small values", 0.0000005,  // Interpolated: 0 + 0.5 * 0.000001
            smallSeq.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), 1e-15);
    }

    @Test
    public void testOutputPortConfiguration() {
        // Verify output port is properly configured
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertFalse("RepeatingSequence block should not have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testSequence", "testSequence", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be RepeatingSequence", "RepeatingSequence", block.getBlockType());
    }

    @Test
    public void testNoInputPorts() {
        assertEquals("RepeatingSequence block should have 0 input ports", 0, block.getInputPortList().size());
    }

    @Test
    public void testParameterCount() {
        // RepeatingSequence block should have parameters for time values, output values, sampleTime, etc.
        assertTrue("Should have at least 2 parameters", block.getParameterList().size() >= 2);
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        block.calculateInit();

        double testTime = 0.5;
        double expected = 0.5;

        // Call multiple times with same time value
        for (int i = 0; i < 50; i++) {
            block.calculateOutput(testTime);
            double output = getScalarOutput(0);
            assertEquals("RepeatingSequence output should be consistent at iteration " + i, expected, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        block.calculateInit();

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average RepeatingSequence calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // RepeatingSequence block should be reasonably fast (lookup and interpolation)
        assertTrue("RepeatingSequence calculateOutput should be fast", avgTime < 15000); // <15 microseconds
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        block.calculateInit();
        block.calculateOutput(0.5);
        double firstOutput = getScalarOutput(0);

        block.calculateOutput(1.5);
        double secondOutput = getScalarOutput(0);

        // Reinitialize
        block.calculateInit();
        block.calculateOutput(0.5);
        double reinitOutput = getScalarOutput(0);

        assertEquals("Output should be same after reinitialization", firstOutput, reinitOutput, DELTA);
        assertEquals("Both interpolated values should be 0.5", 0.5, firstOutput, DELTA);
        assertEquals("Both interpolated values should be 0.5", 0.5, secondOutput, DELTA);
    }

    @Test
    public void testSequentialBlocks() throws Exception {
        // Test creating multiple repeating sequence blocks with different configurations
        RepeatingSequence seq1 = RepeatingSequence.create("seq1", "test", "[0 1]", "[0 1]", mockModel);
        RepeatingSequence seq2 = RepeatingSequence.create("seq2", "test", "[0 0.5 1]", "[1 0 1]", mockModel);
        RepeatingSequence seq3 = RepeatingSequence.create("seq3", "test", "[0 2 4]", "[0 5 0]", mockModel);

        initializeOutputSignals(seq1);
        initializeOutputSignals(seq2);
        initializeOutputSignals(seq3);

        seq1.calculateInit();
        seq2.calculateInit();
        seq3.calculateInit();

        // Test at t=0.5 instead of t=1.0 to avoid period wrapping issues
        double testTime = 0.5;
        seq1.calculateOutput(testTime);
        seq2.calculateOutput(testTime);
        seq3.calculateOutput(testTime);

        // seq1: linear ramp from 0 to 1, at t=0.5 → 0.5
        assertEquals("Seq1 should output 0.5", 0.5,
            seq1.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // seq2: triangle wave [0 0.5 1], [1 0 1], at t=0.5 → 0 (exact match)
        assertEquals("Seq2 should output 0.0", 0.0,
            seq2.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // seq3: at t=0.5, interpolate between t=0 (output=0) and t=2 (output=5) → 0.5/2 * 5 = 1.25
        assertEquals("Seq3 should output 1.25", 1.25,
            seq3.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testNoDriftOverTime() {
        // Verify that output doesn't drift over many iterations
        block.calculateInit();

        double testTime = 0.5;
        double expected = 0.5;

        // Test consistency over many iterations
        for (int i = 0; i < 1000; i++) {
            block.calculateOutput(testTime);
            double output = getScalarOutput(0);
            assertEquals("RepeatingSequence output should not drift at iteration " + i, expected, output, DELTA);
        }
    }

    @Test
    public void testBoundaryConditions() {
        // Test output at exact boundary time points

        // At start of sequence
        block.calculateOutput(0.0);
        assertEquals("Should output 0.0 at start", 0.0, getScalarOutput(0), DELTA);

        // At end of sequence (start of next period)
        block.calculateOutput(2.0);
        assertEquals("Should output 0.0 at end/start", 0.0, getScalarOutput(0), DELTA);

        // Just before end of first period
        block.calculateOutput(1.999);
        double output = getScalarOutput(0);
        assertTrue("Should be close to 0.0 just before period end", Math.abs(output - 0.0) < 0.01);
    }

    @Test
    public void testLongSimulation() {
        // Test sequence over long simulation time
        block.calculateInit();

        double[] testTimes = {0.0, 10.5, 20.0, 50.5, 100.0, 200.5};

        for (double time : testTimes) {
            block.calculateOutput(time);
            double output = getScalarOutput(0);

            // Calculate expected output based on modulo arithmetic
            // Period is 2.0, so t % 2.0 gives position in sequence
            double normalizedTime = time % 2.0;
            double expected;
            if (normalizedTime <= 1.0) {
                expected = normalizedTime; // Linear from 0 to 1
            } else {
                expected = 2.0 - normalizedTime; // Linear from 1 to 0
            }

            assertEquals("Sequence should repeat correctly at t=" + time, expected, output, DELTA);
        }
    }

    @Test
    public void testTimeZero() {
        // Test that t=0 gives first output value
        block.calculateOutput(0.0);
        assertEquals("Should output first value at t=0", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testPreciseInterpolation() throws Exception {
        // Test precise linear interpolation with known values
        // Period is 10, so times wrap at t=10
        RepeatingSequence preciseSeq = RepeatingSequence.create("precise", "test", "[0 10]", "[0 10]", mockModel);
        initializeOutputSignals(preciseSeq);
        preciseSeq.calculateInit();

        // Test various interpolation points
        double[][] testCases = {
            {0.0, 0.0},
            {2.5, 2.5},
            {5.0, 5.0},
            {7.5, 7.5},
            {9.9, 9.9},   // Just before period end
            {12.5, 2.5},  // Second period: (12.5-10=2.5)
            {15.0, 5.0},  // Second period: (15-10=5.0)
            {20.0, 0.0},  // Third period: (20-20=0.0), wraps to t=0
            {25.5, 5.5}   // Third period: (25.5-20=5.5)
        };

        for (double[] testCase : testCases) {
            double time = testCase[0];
            double expected = testCase[1];
            preciseSeq.calculateOutput(time);
            assertEquals("Interpolation mismatch at t=" + time, expected,
                preciseSeq.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
        }
    }

    @Test
    public void testInitializationValue() {
        // Test that calculateInit() sets output to first value in sequence
        block.calculateInit();
        assertEquals("Initial output should be first value in sequence", 0.0, getScalarOutput(0), DELTA);
    }
}
