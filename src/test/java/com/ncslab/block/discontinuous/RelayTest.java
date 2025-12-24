package com.ncslab.block.discontinuous;

import static org.junit.Assert.*;

import org.junit.Test;
import Jama.Matrix;

import com.ncslab.block.Block;
import com.ncslab.block.DirectBlockTestBase;

/**
 * Comprehensive test suite for Relay block using modern mock-based testing.
 *
 * Tests relay switching with hysteresis between on/off states based on
 * configurable switch points and output values.
 *
 * @author NCSLab Team
 * @version 2025
 */
public class RelayTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Default: switchOn=1.0, switchOff=0.0, outputOn=1.0, outputOff=0.0
        return Relay.create("testRelay", "test", "1.0", "0.0", "1.0", "0.0", mockModel);
    }

    // ============================================================================
    // BASIC FUNCTIONALITY TESTS
    // ============================================================================

    @Test
    public void testSwitchOn() {
        setScalarInput(0, 2.0);
        block.calculateOutput(0.0);
        assertEquals("Input above switch-on point should output on-value",
            1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSwitchOff() {
        // First switch on
        setScalarInput(0, 2.0);
        block.calculateOutput(0.0);

        // Then switch off
        setScalarInput(0, -1.0);
        block.calculateOutput(0.0);
        assertEquals("Input below switch-off point should output off-value",
            0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testHysteresis() {
        // Start in off state (below off point)
        setScalarInput(0, -1.0);
        block.calculateOutput(0.0);
        assertEquals("Should be in off state", 0.0, getScalarOutput(0), DELTA);

        // Move to hysteresis zone (between off and on)
        setScalarInput(0, 0.5);
        block.calculateOutput(0.0);
        assertEquals("Should maintain off state in hysteresis zone",
            0.0, getScalarOutput(0), DELTA);

        // Switch on
        setScalarInput(0, 1.5);
        block.calculateOutput(0.0);
        assertEquals("Should switch to on state", 1.0, getScalarOutput(0), DELTA);

        // Return to hysteresis zone
        setScalarInput(0, 0.5);
        block.calculateOutput(0.0);
        assertEquals("Should maintain on state in hysteresis zone",
            1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testInputAtSwitchOnPoint() {
        setScalarInput(0, 1.0);
        block.calculateOutput(0.0);
        assertEquals("Input at switch-on point should output on-value",
            1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testInputAtSwitchOffPoint() {
        // First switch on
        setScalarInput(0, 2.0);
        block.calculateOutput(0.0);

        // Then test at switch-off point
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        assertEquals("Input at switch-off point should output off-value",
            0.0, getScalarOutput(0), DELTA);
    }

    // ============================================================================
    // HYSTERESIS BEHAVIOR TESTS
    // ============================================================================

    @Test
    public void testMultipleSwitchCycles() {
        // Cycle 1: Off -> On
        setScalarInput(0, -1.0);
        block.calculateOutput(0.0);
        assertEquals("Cycle 1: Should be off", 0.0, getScalarOutput(0), DELTA);

        setScalarInput(0, 2.0);
        block.calculateOutput(0.0);
        assertEquals("Cycle 1: Should switch on", 1.0, getScalarOutput(0), DELTA);

        // Cycle 2: On -> Off -> On
        setScalarInput(0, -1.0);
        block.calculateOutput(0.0);
        assertEquals("Cycle 2: Should switch off", 0.0, getScalarOutput(0), DELTA);

        setScalarInput(0, 2.0);
        block.calculateOutput(0.0);
        assertEquals("Cycle 2: Should switch on again", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testStateRetentionInHysteresisZone() {
        // Start in off state
        setScalarInput(0, -1.0);
        block.calculateOutput(0.0);

        // Multiple readings in hysteresis zone should maintain off state
        double[] hysteresisValues = {0.1, 0.3, 0.5, 0.7, 0.9};
        for (double value : hysteresisValues) {
            setScalarInput(0, value);
            block.calculateOutput(0.0);
            assertEquals("Should maintain off state at " + value,
                0.0, getScalarOutput(0), DELTA);
        }

        // Switch on
        setScalarInput(0, 1.5);
        block.calculateOutput(0.0);
        assertEquals("Should switch on", 1.0, getScalarOutput(0), DELTA);

        // Multiple readings in hysteresis zone should maintain on state
        for (double value : hysteresisValues) {
            setScalarInput(0, value);
            block.calculateOutput(0.0);
            assertEquals("Should maintain on state at " + value,
                1.0, getScalarOutput(0), DELTA);
        }
    }

    // ============================================================================
    // EDGE CASE TESTS
    // ============================================================================

    @Test
    public void testVeryLargePositiveInput() {
        setScalarInput(0, 1e10);
        block.calculateOutput(0.0);
        assertEquals("Very large positive input should switch on",
            1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testVeryLargeNegativeInput() {
        setScalarInput(0, -1e10);
        block.calculateOutput(0.0);
        assertEquals("Very large negative input should switch off",
            0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testRapidSwitching() {
        // Rapid alternation between on and off
        for (int i = 0; i < 10; i++) {
            setScalarInput(0, 2.0);
            block.calculateOutput(0.0);
            assertEquals("Iteration " + i + ": Should be on",
                1.0, getScalarOutput(0), DELTA);

            setScalarInput(0, -1.0);
            block.calculateOutput(0.0);
            assertEquals("Iteration " + i + ": Should be off",
                0.0, getScalarOutput(0), DELTA);
        }
    }

    // ============================================================================
    // CUSTOM OUTPUT VALUE TESTS
    // ============================================================================

    @Test
    public void testCustomOutputValues() throws Exception {
        // switchOn=1.0, switchOff=0.0, outputOn=5.0, outputOff=-5.0
        Block customRelay = Relay.create("testRelay", "test",
            "1.0", "0.0", "5.0", "-5.0", mockModel);
        initializeOutputSignals(customRelay);

        // Switch on
        setScalarInputForBlock(customRelay, 0, 2.0);
        customRelay.calculateOutput(0.0);
        assertEquals("Custom on-value should be 5.0",
            5.0, getScalarOutputForBlock(customRelay, 0), DELTA);

        // Switch off
        setScalarInputForBlock(customRelay, 0, -1.0);
        customRelay.calculateOutput(0.0);
        assertEquals("Custom off-value should be -5.0",
            -5.0, getScalarOutputForBlock(customRelay, 0), DELTA);
    }

    @Test
    public void testNegativeOutputValues() throws Exception {
        // switchOn=1.0, switchOff=0.0, outputOn=-1.0, outputOff=-10.0
        Block customRelay = Relay.create("testRelay", "test",
            "1.0", "0.0", "-1.0", "-10.0", mockModel);
        initializeOutputSignals(customRelay);

        // Switch on
        setScalarInputForBlock(customRelay, 0, 2.0);
        customRelay.calculateOutput(0.0);
        assertEquals("Negative on-value should be -1.0",
            -1.0, getScalarOutputForBlock(customRelay, 0), DELTA);

        // Switch off
        setScalarInputForBlock(customRelay, 0, -1.0);
        customRelay.calculateOutput(0.0);
        assertEquals("Negative off-value should be -10.0",
            -10.0, getScalarOutputForBlock(customRelay, 0), DELTA);
    }

    // ============================================================================
    // CUSTOM SWITCH POINT TESTS
    // ============================================================================

    @Test
    public void testAsymmetricSwitchPoints() throws Exception {
        // switchOn=5.0, switchOff=-2.0, outputOn=1.0, outputOff=0.0
        Block customRelay = Relay.create("testRelay", "test",
            "5.0", "-2.0", "1.0", "0.0", mockModel);
        initializeOutputSignals(customRelay);

        // Start below off point
        setScalarInputForBlock(customRelay, 0, -5.0);
        customRelay.calculateOutput(0.0);
        assertEquals("Should be off", 0.0, getScalarOutputForBlock(customRelay, 0), DELTA);

        // In wide hysteresis zone
        setScalarInputForBlock(customRelay, 0, 2.0);
        customRelay.calculateOutput(0.0);
        assertEquals("Should maintain off state in wide hysteresis zone",
            0.0, getScalarOutputForBlock(customRelay, 0), DELTA);

        // Switch on
        setScalarInputForBlock(customRelay, 0, 6.0);
        customRelay.calculateOutput(0.0);
        assertEquals("Should switch on", 1.0, getScalarOutputForBlock(customRelay, 0), DELTA);

        // Return to hysteresis zone
        setScalarInputForBlock(customRelay, 0, 2.0);
        customRelay.calculateOutput(0.0);
        assertEquals("Should maintain on state in hysteresis zone",
            1.0, getScalarOutputForBlock(customRelay, 0), DELTA);
    }

    @Test
    public void testNarrowHysteresis() throws Exception {
        // switchOn=0.1, switchOff=0.0, outputOn=1.0, outputOff=0.0
        Block customRelay = Relay.create("testRelay", "test",
            "0.1", "0.0", "1.0", "0.0", mockModel);
        initializeOutputSignals(customRelay);

        // Test narrow hysteresis band
        setScalarInputForBlock(customRelay, 0, -1.0);
        customRelay.calculateOutput(0.0);
        assertEquals("Should be off", 0.0, getScalarOutputForBlock(customRelay, 0), DELTA);

        setScalarInputForBlock(customRelay, 0, 0.05);
        customRelay.calculateOutput(0.0);
        assertEquals("Should maintain off in narrow hysteresis",
            0.0, getScalarOutputForBlock(customRelay, 0), DELTA);

        setScalarInputForBlock(customRelay, 0, 0.2);
        customRelay.calculateOutput(0.0);
        assertEquals("Should switch on", 1.0, getScalarOutputForBlock(customRelay, 0), DELTA);
    }

    @Test
    public void testNegativeSwitchPoints() throws Exception {
        // switchOn=-1.0, switchOff=-3.0, outputOn=1.0, outputOff=0.0
        Block customRelay = Relay.create("testRelay", "test",
            "-1.0", "-3.0", "1.0", "0.0", mockModel);
        initializeOutputSignals(customRelay);

        // Below off point
        setScalarInputForBlock(customRelay, 0, -5.0);
        customRelay.calculateOutput(0.0);
        assertEquals("Should be off", 0.0, getScalarOutputForBlock(customRelay, 0), DELTA);

        // In hysteresis zone
        setScalarInputForBlock(customRelay, 0, -2.0);
        customRelay.calculateOutput(0.0);
        assertEquals("Should maintain off state",
            0.0, getScalarOutputForBlock(customRelay, 0), DELTA);

        // Above on point
        setScalarInputForBlock(customRelay, 0, 0.0);
        customRelay.calculateOutput(0.0);
        assertEquals("Should switch on", 1.0, getScalarOutputForBlock(customRelay, 0), DELTA);
    }

    // ============================================================================
    // BLOCK METADATA TESTS
    // ============================================================================

    @Test
    public void testBlockName() {
        assertEquals("Block name should match", "testRelay", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be Relay", "Relay", block.getBlockType());
    }

    @Test
    public void testPortConfiguration() {
        assertEquals("Should have 1 input port", 1, block.getInputPortList().size());
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());
    }

    @Test
    public void testParameterCount() {
        // Relay has 7 parameters: OnSwitchValue, OffSwitchValue, OnOutputValue,
        // OffOutputValue, SampleTime, OutDataTypeStr, SaturateOnIntegerOverflow
        assertEquals("Should have 7 parameters", 7, block.getParameterList().size());
    }

    // ============================================================================
    // PERFORMANCE TESTS
    // ============================================================================

    @Test
    public void testCalculateOutputPerformance() {
        setScalarInput(0, 0.5);

        long avgTime = measureCalculateOutputPerformance(1000);

        // Relay should be reasonably fast (< 1 millisecond)
        assertTrue("calculateOutput() should execute in less than 1 millisecond",
            avgTime < 1_000_000);
    }

    @Test
    public void testSwitchingPerformance() {
        // Measure performance of multiple switches
        long startTime = System.nanoTime();

        for (int i = 0; i < 100; i++) {
            setScalarInput(0, 2.0);
            block.calculateOutput(0.0);

            setScalarInput(0, -1.0);
            block.calculateOutput(0.0);
        }

        long endTime = System.nanoTime();
        long avgTimePerSwitch = (endTime - startTime) / 200; // 100 on + 100 off = 200 operations

        System.out.println(String.format("Average relay switching time: %.2f microseconds",
            avgTimePerSwitch / 1000.0));

        // Switching should be reasonably fast
        assertTrue("Relay switching should be reasonably fast (< 1ms per operation)",
            avgTimePerSwitch < 1_000_000); // < 1 millisecond per operation
    }

    // ============================================================================
    // REINITIALIZATION TEST
    // ============================================================================

    @Test
    public void testReinitialization() {
        // Switch on
        setScalarInput(0, 2.0);
        block.calculateOutput(0.0);
        assertEquals("Should be on", 1.0, getScalarOutput(0), DELTA);

        // Reinitialize (should reset to off state)
        try {
            block.calculateInit();
        } catch (Exception e) {
            // Initialization may not be needed
        }

        // After reinitialization, should still work correctly
        setScalarInput(0, -1.0);
        block.calculateOutput(0.0);
        assertEquals("After reinitialization, should be off",
            0.0, getScalarOutput(0), DELTA);

        setScalarInput(0, 2.0);
        block.calculateOutput(0.0);
        assertEquals("After reinitialization, should be able to switch on",
            1.0, getScalarOutput(0), DELTA);
    }

    // ============================================================================
    // STATE CONSISTENCY TESTS
    // ============================================================================

    @Test
    public void testStateConsistencyOverTime() {
        // Once switched on, should maintain state until switched off
        setScalarInput(0, 2.0);
        block.calculateOutput(0.0);

        // Maintain on state over multiple calculations in hysteresis zone
        for (int i = 0; i < 100; i++) {
            setScalarInput(0, 0.5);
            block.calculateOutput(i * 0.01);
            assertEquals("State should remain on over time in hysteresis zone",
                1.0, getScalarOutput(0), DELTA);
        }

        // Switch off
        setScalarInput(0, -1.0);
        block.calculateOutput(0.0);

        // Maintain off state over multiple calculations in hysteresis zone
        for (int i = 0; i < 100; i++) {
            setScalarInput(0, 0.5);
            block.calculateOutput(i * 0.01);
            assertEquals("State should remain off over time in hysteresis zone",
                0.0, getScalarOutput(0), DELTA);
        }
    }

    @Test
    public void testNoOscillationAtBoundaries() {
        // Test that relay doesn't oscillate at exact boundary values

        // Approach from below
        setScalarInput(0, -1.0);
        block.calculateOutput(0.0);

        // Exactly at switch-on point - should switch on
        setScalarInput(0, 1.0);
        block.calculateOutput(0.0);
        double output1 = getScalarOutput(0);

        // Repeat at same point - should maintain state
        setScalarInput(0, 1.0);
        block.calculateOutput(0.0);
        double output2 = getScalarOutput(0);

        assertEquals("Output should not oscillate at switch-on boundary",
            output1, output2, DELTA);
    }

    // ============================================================================
    // COMPREHENSIVE SCENARIO TESTS
    // ============================================================================

    @Test
    public void testComplexSwitchingScenario() {
        // Scenario: Temperature control relay
        // switchOn=25.0, switchOff=20.0, outputOn=1.0 (heater on), outputOff=0.0 (heater off)
        Block tempRelay;
        try {
            tempRelay = Relay.create("tempControl", "test",
                "25.0", "20.0", "1.0", "0.0", mockModel);
            initializeOutputSignals(tempRelay);
        } catch (Exception e) {
            fail("Failed to create temperature control relay: " + e.getMessage());
            return;
        }

        // Room starts cold
        setScalarInputForBlock(tempRelay, 0, 18.0);
        tempRelay.calculateOutput(0.0);
        assertEquals("Heater should be off when temp < switchOff",
            0.0, getScalarOutputForBlock(tempRelay, 0), DELTA);

        // Temperature rises but stays in hysteresis zone
        double[] tempRise = {21.0, 22.0, 23.0, 24.0};
        for (double temp : tempRise) {
            setScalarInputForBlock(tempRelay, 0, temp);
            tempRelay.calculateOutput(0.0);
            assertEquals("Heater should stay off in hysteresis zone at " + temp,
                0.0, getScalarOutputForBlock(tempRelay, 0), DELTA);
        }

        // Temperature reaches switch-on point
        setScalarInputForBlock(tempRelay, 0, 25.0);
        tempRelay.calculateOutput(0.0);
        assertEquals("Heater should switch on at switchOn point",
            1.0, getScalarOutputForBlock(tempRelay, 0), DELTA);

        // Temperature drops but stays in hysteresis zone
        double[] tempDrop = {24.0, 23.0, 22.0, 21.0};
        for (double temp : tempDrop) {
            setScalarInputForBlock(tempRelay, 0, temp);
            tempRelay.calculateOutput(0.0);
            assertEquals("Heater should stay on in hysteresis zone at " + temp,
                1.0, getScalarOutputForBlock(tempRelay, 0), DELTA);
        }

        // Temperature reaches switch-off point
        setScalarInputForBlock(tempRelay, 0, 20.0);
        tempRelay.calculateOutput(0.0);
        assertEquals("Heater should switch off at switchOff point",
            0.0, getScalarOutputForBlock(tempRelay, 0), DELTA);
    }

    @Test
    public void testVeryNarrowHysteresis() throws Exception {
        // switchOn=0.001, switchOff=0.0 (very narrow hysteresis)
        Block narrowHystRelay = Relay.create("testRelay", "test",
            "0.001", "0.0", "1.0", "0.0", mockModel);
        initializeOutputSignals(narrowHystRelay);

        // Below threshold
        setScalarInputForBlock(narrowHystRelay, 0, -0.1);
        narrowHystRelay.calculateOutput(0.0);
        assertEquals("Should be off below threshold",
            0.0, getScalarOutputForBlock(narrowHystRelay, 0), DELTA);

        // In very narrow hysteresis zone
        setScalarInputForBlock(narrowHystRelay, 0, 0.0005);
        narrowHystRelay.calculateOutput(0.0);
        assertEquals("Should maintain off state in narrow hysteresis zone",
            0.0, getScalarOutputForBlock(narrowHystRelay, 0), DELTA);

        // Above threshold
        setScalarInputForBlock(narrowHystRelay, 0, 0.01);
        narrowHystRelay.calculateOutput(0.0);
        assertEquals("Should be on above threshold",
            1.0, getScalarOutputForBlock(narrowHystRelay, 0), DELTA);
    }
}
