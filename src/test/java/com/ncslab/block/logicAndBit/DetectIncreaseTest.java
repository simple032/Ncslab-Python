package com.ncslab.block.logicAndBit;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import Jama.Matrix;
import static org.junit.Assert.*;

/**
 * Test suite for DetectIncrease block using DirectBlockTestBase pattern.
 * Tests edge detection logic with state-based sequential calculations.
 *
 * DetectIncrease outputs VinWhenRising (default 1.0) when input increases,
 * otherwise outputs VinWhenFalling (default 0.0).
 */
public class DetectIncreaseTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Default block: VinWhenRising=1.0, VinWhenFalling=0.0, InitialState=0.0
        return DetectIncrease.create("testDetectIncrease", "test", mockModel);
    }

    // ===== Basic State Transition Tests =====

    @Test
    public void testInitialState_NoIncrease() {
        // After calculateInit(), previousData is set to initialState (0.0)
        // So 5.0 > 0.0 should detect increase
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);
        assertEquals("First call after init - 5.0 > initialState(0.0) = increase", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testIncreaseDetected() {
        // Initialize state
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);
        assertEquals("First call - 5.0 > initialState(0.0) = increase", 1.0, getScalarOutput(0), DELTA);

        // Value increases again
        setScalarInput(0, 10.0);
        block.calculateOutput(0.1);
        assertEquals("Value increased from 5 to 10 - should detect", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNoChange_NoIncrease() {
        // Initialize state
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);

        // Value stays same
        setScalarInput(0, 5.0);
        block.calculateOutput(0.1);
        assertEquals("No change - no increase", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testDecrease_NoIncrease() {
        // Initialize state
        setScalarInput(0, 10.0);
        block.calculateOutput(0.0);

        // Value decreases
        setScalarInput(0, 5.0);
        block.calculateOutput(0.1);
        assertEquals("Value decreased - no increase", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testMultipleIncreasesInSequence() {
        // Initialize
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);

        // First increase
        setScalarInput(0, 5.0);
        block.calculateOutput(0.1);
        assertEquals("First increase detected", 1.0, getScalarOutput(0), DELTA);

        // Second increase
        setScalarInput(0, 10.0);
        block.calculateOutput(0.2);
        assertEquals("Second increase detected", 1.0, getScalarOutput(0), DELTA);

        // Third increase
        setScalarInput(0, 15.0);
        block.calculateOutput(0.3);
        assertEquals("Third increase detected", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testAlternatingIncreaseDecrease() {
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);

        setScalarInput(0, 10.0);
        block.calculateOutput(0.1);
        assertEquals("Increase detected", 1.0, getScalarOutput(0), DELTA);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.2);
        assertEquals("Decrease - no increase", 0.0, getScalarOutput(0), DELTA);

        setScalarInput(0, 15.0);
        block.calculateOutput(0.3);
        assertEquals("Increase detected again", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Zero Crossing Tests =====

    @Test
    public void testZeroCrossing_NegativeToZero() {
        setScalarInput(0, -5.0);
        block.calculateOutput(0.0);

        setScalarInput(0, 0.0);
        block.calculateOutput(0.1);
        assertEquals("Negative to zero is increase", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testZeroCrossing_ZeroToPositive() {
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.1);
        assertEquals("Zero to positive is increase", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testZeroCrossing_NegativeToPositive() {
        setScalarInput(0, -5.0);
        block.calculateOutput(0.0);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.1);
        assertEquals("Negative to positive is increase", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testZeroCrossing_ZeroToNegative() {
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);

        setScalarInput(0, -5.0);
        block.calculateOutput(0.1);
        assertEquals("Zero to negative is not increase", 0.0, getScalarOutput(0), DELTA);
    }

    // ===== Edge Case Tests =====

    @Test
    public void testSmallIncrease() {
        setScalarInput(0, 1.0);
        block.calculateOutput(0.0);

        setScalarInput(0, 1.0 + 1e-10);
        block.calculateOutput(0.1);
        assertEquals("Small increase should be detected", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLargeIncrease() {
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);

        setScalarInput(0, 1e10);
        block.calculateOutput(0.1);
        assertEquals("Large increase should be detected", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testVerySmallIncrease_Precision() {
        setScalarInput(0, 1.0);
        block.calculateOutput(0.0);

        // Increase by smallest distinguishable amount
        setScalarInput(0, 1.0 + Math.ulp(1.0));
        block.calculateOutput(0.1);
        assertEquals("Smallest distinguishable increase", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNegativeValueIncrease() {
        setScalarInput(0, -10.0);
        block.calculateOutput(0.0);

        setScalarInput(0, -5.0);
        block.calculateOutput(0.1);
        assertEquals("Increase from -10 to -5", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Custom Output Values Tests =====

    @Test
    public void testCustomOutputValues() {
        // VinWhenRising=5.0, VinWhenFalling=2.0
        block = DetectIncrease.create("testCustom", "test", "5.0", "2.0", -1.0,
            "Inherit: Logical (see Configuration Parameters: Optimization)", false, "0", mockModel);
        initializeOutputSignals(block);
        block.calculateInit(); // Initialize the block to set previousData

        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);

        setScalarInput(0, 10.0);
        block.calculateOutput(0.1);
        assertEquals("Custom rising value", 5.0, getScalarOutput(0), DELTA);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.2);
        assertEquals("Custom falling value", 2.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNegativeOutputValues() {
        // VinWhenRising=-1.0, VinWhenFalling=-2.0
        block = DetectIncrease.create("testNegative", "test", "-1.0", "-2.0", -1.0,
            "Inherit: Logical (see Configuration Parameters: Optimization)", false, "0", mockModel);
        initializeOutputSignals(block);
        block.calculateInit(); // Initialize the block to set previousData

        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);

        setScalarInput(0, 10.0);
        block.calculateOutput(0.1);
        assertEquals("Negative rising value", -1.0, getScalarOutput(0), DELTA);
    }

    // ===== State Reset Tests (calculateInit) =====

    @Test
    public void testStateResetAfterCalculateInit() {
        // Build up state
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);

        setScalarInput(0, 10.0);
        block.calculateOutput(0.1);
        assertEquals("Increase detected", 1.0, getScalarOutput(0), DELTA);

        // Reset state - previousData is set to initialState (0.0)
        block.calculateInit();

        // After reset, 15.0 > initialState(0.0) = increase
        setScalarInput(0, 15.0);
        block.calculateOutput(0.2);
        assertEquals("After reset - 15.0 > initialState(0.0) = increase", 1.0, getScalarOutput(0), DELTA);

        // Second call after reset should detect increase
        setScalarInput(0, 20.0);
        block.calculateOutput(0.3);
        assertEquals("After reset - second call detects increase", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testInitialStateParameter() {
        // InitialState=10.0
        block = DetectIncrease.create("testInitial", "test", "1.0", "0.0", -1.0,
            "Inherit: Logical (see Configuration Parameters: Optimization)", false, "10.0", mockModel);
        initializeOutputSignals(block);

        // First call with value > initialState
        setScalarInput(0, 15.0);
        block.calculateOutput(0.0);
        // On first call, previousData is set to initialState (10.0)
        // So 15 > 10 should detect increase
        assertEquals("First call with value > initialState", 0.0, getScalarOutput(0), DELTA);

        // Second call
        setScalarInput(0, 20.0);
        block.calculateOutput(0.1);
        assertEquals("Second call - increase from 15 to 20", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Matrix Input Tests =====

    @Test
    public void testMatrixInput_Increase() {
        Matrix initial = new Matrix(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix increased = new Matrix(new double[][]{{2.0, 3.0}, {4.0, 5.0}});

        setMatrixInput(0, initial);
        block.calculateOutput(0.0);
        Matrix result1 = getMatrixOutput(0);
        // First call - after init, previousData is scalar 0.0, so comparison falls through to default
        // All elements compared against initialState (0.0), so all should show increase
        assertEquals("First call element [0,0] - 1.0 > 0.0", 1.0, result1.get(0, 0), DELTA);
        assertEquals("First call element [1,1] - 4.0 > 0.0", 1.0, result1.get(1, 1), DELTA);

        setMatrixInput(0, increased);
        block.calculateOutput(0.1);
        Matrix result2 = getMatrixOutput(0);
        // All elements increased
        assertEquals("Increased element [0,0]", 1.0, result2.get(0, 0), DELTA);
        assertEquals("Increased element [0,1]", 1.0, result2.get(0, 1), DELTA);
        assertEquals("Increased element [1,0]", 1.0, result2.get(1, 0), DELTA);
        assertEquals("Increased element [1,1]", 1.0, result2.get(1, 1), DELTA);
    }

    @Test
    public void testMatrixInput_MixedChanges() {
        // Start with negative values to ensure all initial elements < initialState
        Matrix initial = new Matrix(new double[][]{{-5.0, 0.0}, {5.0, 10.0}});
        setMatrixInput(0, initial);
        block.calculateOutput(0.0);
        Matrix result1 = getMatrixOutput(0);
        // First call compares to initialState (0.0)
        assertEquals("[0,0] -5.0 < 0.0 = no increase", 0.0, result1.get(0, 0), DELTA);
        assertEquals("[0,1] 0.0 == 0.0 = no increase", 0.0, result1.get(0, 1), DELTA);
        assertEquals("[1,0] 5.0 > 0.0 = increase", 1.0, result1.get(1, 0), DELTA);
        assertEquals("[1,1] 10.0 > 0.0 = increase", 1.0, result1.get(1, 1), DELTA);

        // Now test mixed changes from initial values
        Matrix mixed = new Matrix(new double[][]{{5.0, 0.0}, {2.0, 15.0}});
        setMatrixInput(0, mixed);
        block.calculateOutput(0.1);
        Matrix result2 = getMatrixOutput(0);

        assertEquals("[0,0] increased: -5->5", 1.0, result2.get(0, 0), DELTA);
        assertEquals("[0,1] no change: 0->0", 0.0, result2.get(0, 1), DELTA);
        assertEquals("[1,0] decreased: 5->2", 0.0, result2.get(1, 0), DELTA);
        assertEquals("[1,1] increased: 10->15", 1.0, result2.get(1, 1), DELTA);
    }

    // ===== Standard Block Tests =====

    @Test
    public void testPortConfiguration() {
        assertEquals("Should have 1 input port", 1, block.getInputPortList().size());
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());
    }

    @Test
    public void testBlockMetadata() {
        assertNotNull("Block name should not be null", block.getBlockName());
        assertNotNull("Block path should not be null", block.getBlockPath());
        assertEquals("Block type should be DetectIncrease", "DetectIncrease", block.getBlockType());
    }

    @Test
    public void testPerformance() {
        setScalarInput(0, 5.0);
        long avgTime = measureCalculateOutputPerformance(10000);
        assertTrue("calculateOutput should complete in reasonable time", avgTime < 100000); // 100 microseconds
        System.out.println("DetectIncrease performance: " + (avgTime / 1000.0) + " microseconds");
    }
}
