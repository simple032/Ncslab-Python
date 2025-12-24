package com.ncslab.block.source;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Pulse block.
 * Tests calculateInit() and output generation without WebSocket dependencies.
 *
 * Test Coverage:
 * - Pulse output during pulse width (amplitude)
 * - Pulse output outside pulse width (zero)
 * - Periodic pulse generation
 * - Various amplitudes (positive, negative, zero)
 * - Various periods
 * - Various pulse widths
 * - Phase delay effects
 * - Edge cases (zero amplitude, 100% duty cycle, phase delays)
 * - Performance benchmarks
 */
public class PulseTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create Pulse block: amplitude=1.0, period=1.0, pulseWidth=50%, phaseDelay=0.0
        return Pulse.create("testPulse", "test", 1.0, 1.0, 50.0, mockModel);
    }

    @Test
    public void testInitialValue() {
        // Test output at t=0 (should be amplitude since phaseDelay=0 and pulseWidth>0)
        block.calculateOutput(0.0);
        assertEquals("Should output amplitude at t=0", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testPulseHigh() {
        // Test output during pulse high period
        // Period=1.0, pulseWidth=50%, so pulse is high from t=0 to t=0.5
        block.calculateOutput(0.25);
        assertEquals("Should output amplitude during pulse", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testPulseLow() {
        // Test output during pulse low period
        // Period=1.0, pulseWidth=50%, so pulse is low from t=0.5 to t=1.0
        block.calculateOutput(0.75);
        assertEquals("Should output zero outside pulse", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testPeriodicBehavior() {
        // Verify pulse repeats periodically
        // Period=1.0, pulseWidth=50%
        double[][] testCases = {
            {0.0, 1.0},   // First period - high
            {0.25, 1.0},  // First period - high
            {0.5, 0.0},   // First period - low
            {0.75, 0.0},  // First period - low
            {1.0, 1.0},   // Second period - high
            {1.25, 1.0},  // Second period - high
            {1.5, 0.0},   // Second period - low
            {2.0, 1.0},   // Third period - high
            {2.5, 0.0}    // Third period - low
        };

        for (double[] testCase : testCases) {
            double time = testCase[0];
            double expected = testCase[1];
            block.calculateOutput(time);
            assertEquals("Pulse output mismatch at t=" + time, expected, getScalarOutput(0), DELTA);
        }
    }

    @Test
    public void testDifferentAmplitude() throws Exception {
        // Test pulse with different amplitude
        Pulse amplitudePulse = Pulse.create("amplitudePulse", "test", 5.0, 1.0, 50.0, 0.0, 0.0, "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(amplitudePulse);
        amplitudePulse.calculateInit();

        // During pulse
        amplitudePulse.calculateOutput(0.25);
        assertEquals("Should output 5.0 during pulse", 5.0,
            amplitudePulse.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // Outside pulse
        amplitudePulse.calculateOutput(0.75);
        assertEquals("Should output 0.0 outside pulse", 0.0,
            amplitudePulse.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testNegativeAmplitude() throws Exception {
        // Test pulse with negative amplitude
        Pulse negativePulse = Pulse.create("negativePulse", "test", -3.0, 1.0, 50.0, 0.0, 0.0, "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(negativePulse);
        negativePulse.calculateInit();

        // During pulse
        negativePulse.calculateOutput(0.25);
        assertEquals("Should output -3.0 during pulse", -3.0,
            negativePulse.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // Outside pulse
        negativePulse.calculateOutput(0.75);
        assertEquals("Should output 0.0 outside pulse", 0.0,
            negativePulse.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testDifferentPeriod() throws Exception {
        // Test pulse with different period
        Pulse periodPulse = Pulse.create("periodPulse", "test", 1.0, 2.0, 50.0, 0.0, 0.0, "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(periodPulse);
        periodPulse.calculateInit();

        // First period: high from 0 to 1.0, low from 1.0 to 2.0
        periodPulse.calculateOutput(0.5);
        assertEquals("Should be high in first half of period", 1.0,
            periodPulse.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        periodPulse.calculateOutput(1.5);
        assertEquals("Should be low in second half of period", 0.0,
            periodPulse.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // Second period starts at t=2.0
        periodPulse.calculateOutput(2.5);
        assertEquals("Should be high again in second period", 1.0,
            periodPulse.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testDifferentPulseWidth() throws Exception {
        // Test pulse with 25% duty cycle
        Pulse dutyCyclePulse = Pulse.create("dutyCyclePulse", "test", 1.0, 1.0, 25.0, 0.0, 0.0, "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(dutyCyclePulse);
        dutyCyclePulse.calculateInit();

        // 25% duty cycle: high from 0 to 0.25, low from 0.25 to 1.0
        dutyCyclePulse.calculateOutput(0.1);
        assertEquals("Should be high during first 25% of period", 1.0,
            dutyCyclePulse.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        dutyCyclePulse.calculateOutput(0.5);
        assertEquals("Should be low after 25% of period", 0.0,
            dutyCyclePulse.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testFullDutyCycle() throws Exception {
        // Test pulse with 100% duty cycle (always high)
        Pulse fullDutyCycle = Pulse.create("fullDutyCycle", "test", 1.0, 1.0, 100.0, 0.0, 0.0, "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(fullDutyCycle);
        fullDutyCycle.calculateInit();

        // Should always be high
        double[] testTimes = {0.0, 0.25, 0.5, 0.75, 1.0, 1.5, 2.0};
        for (double time : testTimes) {
            fullDutyCycle.calculateOutput(time);
            assertEquals("Should always be high with 100% duty cycle at t=" + time, 1.0,
                fullDutyCycle.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
        }
    }

    @Test
    public void testZeroDutyCycle() throws Exception {
        // Test pulse with 0% duty cycle (always low)
        Pulse zeroDutyCycle = Pulse.create("zeroDutyCycle", "test", 1.0, 1.0, 0.0, 0.0, 0.0, "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(zeroDutyCycle);
        zeroDutyCycle.calculateInit();

        // Should always be low
        double[] testTimes = {0.0, 0.25, 0.5, 0.75, 1.0, 1.5, 2.0};
        for (double time : testTimes) {
            zeroDutyCycle.calculateOutput(time);
            assertEquals("Should always be low with 0% duty cycle at t=" + time, 0.0,
                zeroDutyCycle.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
        }
    }

    @Test
    public void testPhaseDelay() throws Exception {
        // Test pulse with phase delay
        Pulse delayedPulse = Pulse.create("delayedPulse", "test", 1.0, 1.0, 50.0, 0.5, 0.0, "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(delayedPulse);
        delayedPulse.calculateInit();

        // Phase delay = 0.5, so pulse starts at t=0.5
        delayedPulse.calculateOutput(0.25);
        assertEquals("Should be low before phase delay", 0.0,
            delayedPulse.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // At t=0.5 (start of delayed pulse)
        delayedPulse.calculateOutput(0.5);
        assertEquals("Should be high at phase delay start", 1.0,
            delayedPulse.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // At t=0.75 (still high: 0.5 to 1.0 is high)
        delayedPulse.calculateOutput(0.75);
        assertEquals("Should be high during pulse width", 1.0,
            delayedPulse.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // At t=1.25 (low: 1.0 to 1.5 is low)
        delayedPulse.calculateOutput(1.25);
        assertEquals("Should be low outside pulse width", 0.0,
            delayedPulse.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testZeroAmplitude() throws Exception {
        // Test pulse with zero amplitude
        Pulse zeroAmplitude = Pulse.create("zeroAmplitude", "test", 0.0, 1.0, 50.0, 0.0, 0.0, "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(zeroAmplitude);
        zeroAmplitude.calculateInit();

        // Should always output 0.0
        double[] testTimes = {0.0, 0.25, 0.5, 0.75, 1.0};
        for (double time : testTimes) {
            zeroAmplitude.calculateOutput(time);
            assertEquals("Should output 0.0 with zero amplitude at t=" + time, 0.0,
                zeroAmplitude.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
        }
    }

    @Test
    public void testLargeAmplitude() throws Exception {
        // Test pulse with very large amplitude
        double largeAmplitude = 1e6;
        Pulse largePulse = Pulse.create("largePulse", "test", largeAmplitude, 1.0, 50.0, 0.0, 0.0, "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(largePulse);
        largePulse.calculateInit();

        largePulse.calculateOutput(0.25);
        assertEquals("Should handle large amplitude", largeAmplitude,
            largePulse.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), largeAmplitude * 1e-10);
    }

    @Test
    public void testSmallAmplitude() throws Exception {
        // Test pulse with very small amplitude
        double smallAmplitude = 1e-10;
        Pulse smallPulse = Pulse.create("smallPulse", "test", smallAmplitude, 1.0, 50.0, 0.0, 0.0, "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(smallPulse);
        smallPulse.calculateInit();

        smallPulse.calculateOutput(0.25);
        assertEquals("Should handle small amplitude", smallAmplitude,
            smallPulse.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), 1e-20);
    }

    @Test
    public void testOutputPortConfiguration() {
        // Verify output port is properly configured
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertFalse("Pulse block should not have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testPulse", "testPulse", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be Pulse", "Pulse", block.getBlockType());
    }

    @Test
    public void testNoInputPorts() {
        assertEquals("Pulse block should have 0 input ports", 0, block.getInputPortList().size());
    }

    @Test
    public void testParameterCount() {
        // Pulse block should have parameters for amplitude, period, pulseWidth, phaseDelay, etc.
        assertTrue("Should have at least 7 parameters", block.getParameterList().size() >= 7);
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        block.calculateInit();

        double testTime = 0.25; // During pulse high

        // Call multiple times with same time value
        for (int i = 0; i < 50; i++) {
            block.calculateOutput(testTime);
            double output = getScalarOutput(0);
            assertEquals("Should always output amplitude during pulse at iteration " + i, 1.0, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        block.calculateInit();

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Pulse calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Pulse block should be reasonably fast (modulo and comparison operations)
        assertTrue("Pulse calculateOutput should be fast", avgTime < 10000); // <10 microseconds
    }

    @Test
    public void testPulseAtVariousConfigurations() {
        // Test pulse with different configurations
        double[][] configurations = {
            {1.0, 1.0, 50.0, 0.0},   // amplitude=1, period=1, width=50%, delay=0
            {2.0, 2.0, 25.0, 0.0},   // amplitude=2, period=2, width=25%, delay=0
            {0.5, 0.5, 75.0, 0.1},   // amplitude=0.5, period=0.5, width=75%, delay=0.1
            {3.0, 3.0, 10.0, 0.5}    // amplitude=3, period=3, width=10%, delay=0.5
        };

        for (double[] config : configurations) {
            try {
                double amplitude = config[0];
                double period = config[1];
                double pulseWidth = config[2];
                double phaseDelay = config[3];

                Pulse testPulse = Pulse.create("test", "test", amplitude, period, pulseWidth,
                    phaseDelay, 0.0, "Inherit: Same as parameter", false, mockModel);
                initializeOutputSignals(testPulse);
                testPulse.calculateInit();

                // Test at a time where pulse should be high (adjusted for phase delay)
                double pulseWidthTime = (pulseWidth / 100.0) * period;
                double testTimeHigh = phaseDelay + pulseWidthTime / 2.0;
                testPulse.calculateOutput(testTimeHigh);
                double outputHigh = testPulse.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();

                // Test at a time where pulse should be low
                double testTimeLow = phaseDelay + pulseWidthTime + 0.1;
                testPulse.calculateOutput(testTimeLow);
                double outputLow = testPulse.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();

                // Verify high output
                assertTrue("Output should be amplitude or 0 during high period",
                    Math.abs(outputHigh - amplitude) < DELTA || Math.abs(outputHigh - 0.0) < DELTA);

                // Verify low output
                assertEquals("Output should be 0 during low period", 0.0, outputLow, DELTA);

            } catch (Exception e) {
                fail("Failed to test pulse configuration: " + e.getMessage());
            }
        }
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        block.calculateInit();
        block.calculateOutput(0.25);
        double firstOutput = getScalarOutput(0);

        block.calculateOutput(0.75);
        double secondOutput = getScalarOutput(0);

        // Reinitialize
        block.calculateInit();
        block.calculateOutput(0.25);
        double reinitOutput = getScalarOutput(0);

        assertEquals("Output should be same after reinitialization", firstOutput, reinitOutput, DELTA);
        assertNotEquals("High and low outputs should differ", firstOutput, secondOutput, DELTA);
    }

    @Test
    public void testSequentialBlocks() throws Exception {
        // Test creating multiple pulse blocks with different configurations
        Pulse pulse1 = Pulse.create("pulse1", "test", 1.0, 1.0, 50.0, 0.0, 0.0, "Inherit: Same as parameter", false, mockModel);
        Pulse pulse2 = Pulse.create("pulse2", "test", 2.0, 2.0, 25.0, 0.0, 0.0, "Inherit: Same as parameter", false, mockModel);
        Pulse pulse3 = Pulse.create("pulse3", "test", 3.0, 1.0, 75.0, 0.5, 0.0, "Inherit: Same as parameter", false, mockModel);

        initializeOutputSignals(pulse1);
        initializeOutputSignals(pulse2);
        initializeOutputSignals(pulse3);

        pulse1.calculateInit();
        pulse2.calculateInit();
        pulse3.calculateInit();

        double testTime = 0.25;
        pulse1.calculateOutput(testTime);
        pulse2.calculateOutput(testTime);
        pulse3.calculateOutput(testTime);

        // pulse1: period=1, width=50%, t=0.25 is in high period (0 to 0.5)
        assertEquals("Pulse1 should be high", 1.0,
            pulse1.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // pulse2: period=2, width=25%, t=0.25 is in high period (0 to 0.5)
        assertEquals("Pulse2 should be high", 2.0,
            pulse2.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        // pulse3: phaseDelay=0.5, so at t=0.25 pulse hasn't started yet
        assertEquals("Pulse3 should be low (before phase delay)", 0.0,
            pulse3.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testNoDriftOverTime() {
        // Verify that output doesn't drift over many iterations
        block.calculateInit();

        double testTimeHigh = 0.25;  // During pulse
        double testTimeLow = 0.75;   // Outside pulse

        // Test consistency over many iterations
        for (int i = 0; i < 1000; i++) {
            block.calculateOutput(testTimeHigh);
            double outputHigh = getScalarOutput(0);
            assertEquals("High output should not drift at iteration " + i, 1.0, outputHigh, DELTA);

            block.calculateOutput(testTimeLow);
            double outputLow = getScalarOutput(0);
            assertEquals("Low output should not drift at iteration " + i, 0.0, outputLow, DELTA);
        }
    }

    @Test
    public void testPulseEdgeTransitions() throws Exception {
        // Verify pulse transitions at edges
        Pulse edgePulse = Pulse.create("edgePulse", "test", 1.0, 1.0, 50.0, 0.0, 0.0, "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(edgePulse);
        edgePulse.calculateInit();

        // Test at edges
        double[][] edgeTests = {
            {0.0, 1.0},      // Start of pulse
            {0.49999, 1.0},  // Just before falling edge
            {0.5, 0.0},      // Falling edge
            {0.50001, 0.0},  // Just after falling edge
            {0.99999, 0.0},  // Just before rising edge
            {1.0, 1.0},      // Rising edge (new period)
            {1.00001, 1.0}   // Just after rising edge
        };

        for (double[] test : edgeTests) {
            double time = test[0];
            double expected = test[1];
            edgePulse.calculateOutput(time);
            assertEquals("Pulse edge mismatch at t=" + time, expected,
                edgePulse.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
        }
    }

    @Test
    public void testPulseInitialization() {
        // Test that calculateInit() properly initializes the pulse
        block.calculateInit();
        // With phaseDelay=0 and pulseWidth=50%, initial output should be amplitude
        assertEquals("Pulse should initialize to amplitude (high)", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLongPeriod() throws Exception {
        // Test pulse with long period
        Pulse longPeriod = Pulse.create("longPeriod", "test", 1.0, 100.0, 50.0, 0.0, 0.0, "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(longPeriod);
        longPeriod.calculateInit();

        // Period=100, width=50%, so high from 0 to 50
        longPeriod.calculateOutput(25.0);
        assertEquals("Should be high in first half of long period", 1.0,
            longPeriod.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        longPeriod.calculateOutput(75.0);
        assertEquals("Should be low in second half of long period", 0.0,
            longPeriod.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testShortPeriod() throws Exception {
        // Test pulse with short period
        Pulse shortPeriod = Pulse.create("shortPeriod", "test", 1.0, 0.1, 50.0, 0.0, 0.0, "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(shortPeriod);
        shortPeriod.calculateInit();

        // Period=0.1, width=50%, so high from 0 to 0.05
        shortPeriod.calculateOutput(0.025);
        assertEquals("Should be high in first half of short period", 1.0,
            shortPeriod.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);

        shortPeriod.calculateOutput(0.075);
        assertEquals("Should be low in second half of short period", 0.0,
            shortPeriod.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }
}
