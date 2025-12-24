package com.ncslab.block.source;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for SineWave block.
 * Tests calculateInit() and output generation without WebSocket dependencies.
 *
 * Test Coverage:
 * - Sine wave output at various times, amplitudes, frequencies, phases, biases
 * - Mathematical correctness of sine function
 * - Edge cases (zero values, phase shifts, negative frequencies)
 * - Performance benchmarks
 */
public class SineWaveTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create SineWave block: amplitude=1.0, bias=0.0, frequency=1.0 rad/s, phase=0.0, sampleTime=0.0
        return SineWave.create("testSineWave", "test", 1.0, 1.0, mockModel);
    }

    @Test
    public void testInitialOutput() {
        // Test output at t=0 (should be amplitude * sin(phase) + bias = 1.0 * sin(0) + 0 = 0.0)
        assertEquals("Should output 0.0 at t=0 with zero phase", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSineWaveAtQuarterPeriod() {
        // At t=π/2 with frequency=1 rad/s: sin(1*π/2) = 1
        double time = Math.PI / 2.0;
        block.calculateOutput(time);
        assertEquals("Should output amplitude at quarter period", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSineWaveAtHalfPeriod() {
        // At t=π with frequency=1 rad/s: sin(1*π) = 0
        double time = Math.PI;
        block.calculateOutput(time);
        assertEquals("Should output 0.0 at half period", 0.0, getScalarOutput(0), 1e-9);
    }

    @Test
    public void testSineWaveAtThreeQuarterPeriod() {
        // At t=3π/2 with frequency=1 rad/s: sin(1*3π/2) = -1
        double time = 3.0 * Math.PI / 2.0;
        block.calculateOutput(time);
        assertEquals("Should output -amplitude at three quarter period", -1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSineWaveAtFullPeriod() {
        // At t=2π with frequency=1 rad/s: sin(1*2π) = 0
        double time = 2.0 * Math.PI;
        block.calculateOutput(time);
        assertEquals("Should output 0.0 at full period", 0.0, getScalarOutput(0), 1e-9);
    }

    @Test
    public void testMultipleTimePoints() {
        // Verify sine wave behavior across multiple time points
        double[][] testCases = {
            {0.0, 0.0},                          // t=0 → sin(0) = 0
            {Math.PI / 6.0, 0.5},                // t=π/6 → sin(π/6) = 0.5
            {Math.PI / 4.0, Math.sqrt(2) / 2},   // t=π/4 → sin(π/4) = √2/2
            {Math.PI / 3.0, Math.sqrt(3) / 2},   // t=π/3 → sin(π/3) = √3/2
            {Math.PI / 2.0, 1.0},                // t=π/2 → sin(π/2) = 1
            {Math.PI, 0.0},                      // t=π → sin(π) = 0
            {3.0 * Math.PI / 2.0, -1.0},         // t=3π/2 → sin(3π/2) = -1
            {2.0 * Math.PI, 0.0}                 // t=2π → sin(2π) = 0
        };

        for (double[] testCase : testCases) {
            double time = testCase[0];
            double expected = testCase[1];
            block.calculateOutput(time);
            assertEquals("Sine wave output mismatch at t=" + time, expected, getScalarOutput(0), 1e-9);
        }
    }

    @Test
    public void testWithAmplitude() throws Exception {
        // Test sine wave with amplitude=2.0
        SineWave amplitudeWave = SineWave.create("ampWave", "test", 2.0, 1.0, mockModel);
        initializeOutputSignals(amplitudeWave);
        amplitudeWave.calculateInit();

        amplitudeWave.calculateOutput(Math.PI / 2.0);
        assertEquals("Should output 2.0 at quarter period", 2.0,
            amplitudeWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        amplitudeWave.calculateOutput(3.0 * Math.PI / 2.0);
        assertEquals("Should output -2.0 at three quarter period", -2.0,
            amplitudeWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testWithBias() throws Exception {
        // Test sine wave with bias=5.0
        SineWave biasWave = SineWave.create("biasWave", "test", 1.0, 5.0, 1.0, 0.0, 0.0, 1,
            "Use simulation time", "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(biasWave);
        biasWave.calculateInit();

        // At t=0: sin(0) + 5 = 5
        biasWave.calculateOutput(0.0);
        assertEquals("Should output bias at t=0", 5.0,
            biasWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // At t=π/2: sin(π/2) + 5 = 1 + 5 = 6
        biasWave.calculateOutput(Math.PI / 2.0);
        assertEquals("Should output amplitude + bias at quarter period", 6.0,
            biasWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // At t=3π/2: sin(3π/2) + 5 = -1 + 5 = 4
        biasWave.calculateOutput(3.0 * Math.PI / 2.0);
        assertEquals("Should output -amplitude + bias at three quarter period", 4.0,
            biasWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testWithFrequency() throws Exception {
        // Test sine wave with frequency=2.0 rad/s (twice as fast)
        SineWave frequencyWave = SineWave.create("freqWave", "test", 1.0, 0.0, 2.0, 0.0, 0.0, 1,
            "Use simulation time", "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(frequencyWave);
        frequencyWave.calculateInit();

        // At t=π/4 with f=2: sin(2*π/4) = sin(π/2) = 1
        frequencyWave.calculateOutput(Math.PI / 4.0);
        assertEquals("Should reach max amplitude at π/4 with frequency=2", 1.0,
            frequencyWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // At t=π/2 with f=2: sin(2*π/2) = sin(π) = 0
        frequencyWave.calculateOutput(Math.PI / 2.0);
        assertEquals("Should reach zero at π/2 with frequency=2", 0.0,
            frequencyWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), 1e-9);
    }

    @Test
    public void testWithPhase() throws Exception {
        // Test sine wave with phase=π/2 (90 degree phase shift)
        SineWave phaseWave = SineWave.create("phaseWave", "test", 1.0, 0.0, 1.0, Math.PI / 2.0, 0.0, 1,
            "Use simulation time", "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(phaseWave);
        phaseWave.calculateInit();

        // At t=0 with phase=π/2: sin(0 + π/2) = 1
        assertEquals("Should output 1.0 at t=0 with π/2 phase shift", 1.0,
            phaseWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // At t=π/2 with phase=π/2: sin(π/2 + π/2) = sin(π) = 0
        phaseWave.calculateOutput(Math.PI / 2.0);
        assertEquals("Should output 0.0 at π/2 with π/2 phase shift", 0.0,
            phaseWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), 1e-9);
    }

    @Test
    public void testComplexWaveform() throws Exception {
        // Test with amplitude=3.0, bias=2.0, frequency=0.5, phase=π/4
        SineWave complexWave = SineWave.create("complexWave", "test", 3.0, 2.0, 0.5, Math.PI / 4.0, 0.0, 1,
            "Use simulation time", "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(complexWave);
        complexWave.calculateInit();

        // At t=0: 3*sin(0.5*0 + π/4) + 2 = 3*sin(π/4) + 2 = 3*√2/2 + 2 ≈ 4.121
        double expected = 3.0 * Math.sin(Math.PI / 4.0) + 2.0;
        assertEquals("Should output correct complex waveform at t=0", expected,
            complexWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // At t=π: 3*sin(0.5*π + π/4) + 2 = 3*sin(3π/4) + 2 = 3*√2/2 + 2 ≈ 4.121
        complexWave.calculateOutput(Math.PI);
        expected = 3.0 * Math.sin(0.5 * Math.PI + Math.PI / 4.0) + 2.0;
        assertEquals("Should output correct complex waveform at t=π", expected,
            complexWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testZeroAmplitude() throws Exception {
        // Test sine wave with zero amplitude (should always output bias)
        SineWave zeroAmpWave = SineWave.create("zeroAmp", "test", 0.0, 5.0, 1.0, 0.0, 0.0, 1,
            "Use simulation time", "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(zeroAmpWave);
        zeroAmpWave.calculateInit();

        double[] testTimes = {0.0, Math.PI / 2.0, Math.PI, 3.0 * Math.PI / 2.0, 2.0 * Math.PI};
        for (double time : testTimes) {
            zeroAmpWave.calculateOutput(time);
            assertEquals("Should always output bias with zero amplitude", 5.0,
                zeroAmpWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
        }
    }

    @Test
    public void testZeroFrequency() throws Exception {
        // Test sine wave with zero frequency (should be constant at amplitude * sin(phase) + bias)
        SineWave zeroFreqWave = SineWave.create("zeroFreq", "test", 2.0, 1.0, 0.0, Math.PI / 6.0, 0.0, 1,
            "Use simulation time", "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(zeroFreqWave);
        zeroFreqWave.calculateInit();

        double expected = 2.0 * Math.sin(Math.PI / 6.0) + 1.0; // 2 * 0.5 + 1 = 2

        double[] testTimes = {0.0, 1.0, 5.0, 10.0, 100.0};
        for (double time : testTimes) {
            zeroFreqWave.calculateOutput(time);
            assertEquals("Should be constant with zero frequency", expected,
                zeroFreqWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
        }
    }

    @Test
    public void testOutputPortConfiguration() {
        // Verify output port is properly configured
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertFalse("SineWave block should not have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testSineWave", "testSineWave", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be SineWave", "SineWave", block.getBlockType());
    }

    @Test
    public void testNoInputPorts() {
        assertEquals("SineWave block should have 0 input ports", 0, block.getInputPortList().size());
    }

    @Test
    public void testParameterCount() {
        // SineWave block should have parameters for amplitude, bias, frequency, phase, sampleTime, etc.
        assertTrue("Should have at least 5 parameters", block.getParameterList().size() >= 5);
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        block.calculateInit();

        double testTime = Math.PI / 2.0;
        double expected = 1.0;

        // Call multiple times with same time value
        for (int i = 0; i < 50; i++) {
            block.calculateOutput(testTime);
            double output = getScalarOutput(0);
            assertEquals("SineWave output should be consistent at iteration " + i, expected, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        block.calculateInit();

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average SineWave calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // SineWave block should be reasonably fast (trigonometric operation)
        assertTrue("SineWave calculateOutput should be fast", avgTime < 10000); // <10 microseconds
    }

    @Test
    public void testNegativeFrequency() throws Exception {
        // Negative frequency should reverse direction (phase becomes -phase effectively)
        // sin(-ωt + φ) = -sin(ωt - φ)
        SineWave negFreqWave = SineWave.create("negFreq", "test", 1.0, 0.0, -1.0, 0.0, 0.0, 1,
            "Use simulation time", "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(negFreqWave);
        negFreqWave.calculateInit();

        // At t=π/2 with f=-1: sin(-1*π/2) = -1
        negFreqWave.calculateOutput(Math.PI / 2.0);
        assertEquals("Should output -1.0 at quarter period with negative frequency", -1.0,
            negFreqWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testLargeAmplitude() throws Exception {
        // Test sine wave with very large amplitude
        double largeAmplitude = 1e6;
        SineWave largeAmpWave = SineWave.create("largeAmp", "test", largeAmplitude, 0.0, 1.0, 0.0, 0.0, 1,
            "Use simulation time", "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(largeAmpWave);
        largeAmpWave.calculateInit();

        largeAmpWave.calculateOutput(Math.PI / 2.0);
        assertEquals("Should output large amplitude", largeAmplitude,
            largeAmpWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), largeAmplitude * 1e-10);
    }

    @Test
    public void testSmallAmplitude() throws Exception {
        // Test sine wave with very small amplitude
        double smallAmplitude = 1e-10;
        SineWave smallAmpWave = SineWave.create("smallAmp", "test", smallAmplitude, 0.0, 1.0, 0.0, 0.0, 1,
            "Use simulation time", "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(smallAmpWave);
        smallAmpWave.calculateInit();

        smallAmpWave.calculateOutput(Math.PI / 2.0);
        assertEquals("Should output small amplitude", smallAmplitude,
            smallAmpWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), 1e-20);
    }

    @Test
    public void testHighFrequency() throws Exception {
        // Test sine wave with high frequency
        SineWave highFreqWave = SineWave.create("highFreq", "test", 1.0, 0.0, 100.0, 0.0, 0.0, 1,
            "Use simulation time", "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(highFreqWave);
        highFreqWave.calculateInit();

        // At t=π/200 with f=100: sin(100*π/200) = sin(π/2) = 1
        highFreqWave.calculateOutput(Math.PI / 200.0);
        assertEquals("Should handle high frequency correctly", 1.0,
            highFreqWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        block.calculateInit();
        block.calculateOutput(Math.PI / 2.0);
        double firstOutput = getScalarOutput(0);

        block.calculateOutput(Math.PI);
        double secondOutput = getScalarOutput(0);

        // Reinitialize
        block.calculateInit();
        block.calculateOutput(Math.PI / 2.0);
        double reinitOutput = getScalarOutput(0);

        assertEquals("Output should be same after reinitialization", firstOutput, reinitOutput, DELTA);
        assertNotEquals("Outputs at different times should differ", firstOutput, secondOutput, DELTA);
    }

    @Test
    public void testSequentialBlocks() throws Exception {
        // Test creating multiple sine wave blocks with different configurations
        SineWave sine1 = SineWave.create("sine1", "test", 1.0, 1.0, mockModel);
        SineWave sine2 = SineWave.create("sine2", "test", 2.0, 0.5, mockModel);
        SineWave sine3 = SineWave.create("sine3", "test", 0.5, 2.0, mockModel);

        initializeOutputSignals(sine1);
        initializeOutputSignals(sine2);
        initializeOutputSignals(sine3);

        sine1.calculateInit();
        sine2.calculateInit();
        sine3.calculateInit();

        double testTime = Math.PI / 2.0;
        sine1.calculateOutput(testTime);
        sine2.calculateOutput(testTime);
        sine3.calculateOutput(testTime);

        // sine1: amplitude=1, freq=1, at t=π/2: sin(π/2) = 1
        assertEquals("Sine1 should output 1.0", 1.0,
            sine1.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // sine2: amplitude=2, freq=0.5, at t=π/2: 2*sin(0.5*π/2) = 2*sin(π/4) = 2*√2/2 = √2
        assertEquals("Sine2 should output √2", Math.sqrt(2),
            sine2.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // sine3: amplitude=0.5, freq=2, at t=π/2: 0.5*sin(2*π/2) = 0.5*sin(π) = 0
        assertEquals("Sine3 should output 0.0", 0.0,
            sine3.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), 1e-9);
    }

    @Test
    public void testNoDriftOverTime() {
        // Verify that output doesn't drift over many iterations
        block.calculateInit();

        double testTime = Math.PI / 2.0;
        double expected = 1.0;

        // Test consistency over many iterations
        for (int i = 0; i < 1000; i++) {
            block.calculateOutput(testTime);
            double output = getScalarOutput(0);
            assertEquals("SineWave output should not drift at iteration " + i, expected, output, DELTA);
        }
    }

    @Test
    public void testVaryingTimeSequence() {
        // Test sine wave output at incrementing time sequence
        double[] timeSequence = {0.0, 0.1, 0.2, 0.5, 1.0, 2.0, 5.0, 10.0};

        for (double time : timeSequence) {
            block.calculateOutput(time);
            double output = getScalarOutput(0);
            double expected = Math.sin(time); // frequency=1, amplitude=1, bias=0, phase=0
            assertEquals("Sine wave should match sin(t) at t=" + time, expected, output, DELTA);
        }
    }

    @Test
    public void testSineWaveSymmetry() throws Exception {
        // Test that sin(-x) = -sin(x) with appropriate frequency
        SineWave symmetryWave = SineWave.create("symmetry", "test", 1.0, 0.0, 1.0, 0.0, 0.0, 1,
            "Use simulation time", "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(symmetryWave);
        symmetryWave.calculateInit();

        double testTime = 0.5;

        symmetryWave.calculateOutput(testTime);
        double positiveOutput = symmetryWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();

        symmetryWave.calculateOutput(-testTime);
        double negativeOutput = symmetryWave.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();

        assertEquals("sin(-x) should equal -sin(x)", -positiveOutput, negativeOutput, DELTA);
    }

    @Test
    public void testSineWavePeriodicity() {
        // Test that sine wave repeats after period (2π/ω)
        double period = 2.0 * Math.PI; // frequency = 1 rad/s

        double time1 = 0.5;
        block.calculateOutput(time1);
        double output1 = getScalarOutput(0);

        double time2 = time1 + period;
        block.calculateOutput(time2);
        double output2 = getScalarOutput(0);

        assertEquals("Sine wave should repeat after one period", output1, output2, DELTA);
    }

    @Test
    public void testInitializationValue() {
        // Test that calculateInit() sets correct initial value
        // For amplitude=1, bias=0, phase=0: initial value = 1*sin(0) + 0 = 0
        block.calculateInit();
        assertEquals("Initial output should be amplitude*sin(phase) + bias", 0.0, getScalarOutput(0), DELTA);
    }
}
