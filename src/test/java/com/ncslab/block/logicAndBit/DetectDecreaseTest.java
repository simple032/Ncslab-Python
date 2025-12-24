package com.ncslab.block.logicAndBit;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import Jama.Matrix;
import static org.junit.Assert.*;

/**
 * Test suite for DetectDecrease block using DirectBlockTestBase pattern.
 * Tests edge detection logic with state-based sequential calculations.
 *
 * DetectDecrease outputs VinWhenFalling (default 1.0) when input decreases,
 * otherwise outputs VinWhenRising (default 0.0).
 */
public class DetectDecreaseTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Default block: VinWhenRising=0.0, VinWhenFalling=1.0, InitialState=0.0
        return DetectDecrease.create("testDetectDecrease", "test", mockModel);
    }

    // ===== Basic State Transition Tests =====

    @Test
    public void testInitialState_NoDecrease() {
        // After calculateInit(), previousData is set to initialState (0.0)
        // So 5.0 > 0.0 (not a decrease) = risingValue (0.0)
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);
        assertEquals("First call after init - 5.0 > initialState(0.0) = no decrease", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testDecreaseDetected() {
        // Initialize state - 10.0 > initialState(0.0) = no decrease
        setScalarInput(0, 10.0);
        block.calculateOutput(0.0);
        assertEquals("First call - 10.0 > initialState(0.0) = no decrease", 0.0, getScalarOutput(0), DELTA);

        // Value decreases
        setScalarInput(0, 5.0);
        block.calculateOutput(0.1);
        assertEquals("Value decreased from 10 to 5 - should detect", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNoChange_NoDecrease() {
        // Initialize state
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);

        // Value stays same
        setScalarInput(0, 5.0);
        block.calculateOutput(0.1);
        assertEquals("No change - no decrease", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testIncrease_NoDecrease() {
        // Initialize state
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);

        // Value increases
        setScalarInput(0, 10.0);
        block.calculateOutput(0.1);
        assertEquals("Value increased - no decrease", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testMultipleDecreasesInSequence() {
        // Initialize
        setScalarInput(0, 20.0);
        block.calculateOutput(0.0);

        // First decrease
        setScalarInput(0, 15.0);
        block.calculateOutput(0.1);
        assertEquals("First decrease detected", 1.0, getScalarOutput(0), DELTA);

        // Second decrease
        setScalarInput(0, 10.0);
        block.calculateOutput(0.2);
        assertEquals("Second decrease detected", 1.0, getScalarOutput(0), DELTA);

        // Third decrease
        setScalarInput(0, 5.0);
        block.calculateOutput(0.3);
        assertEquals("Third decrease detected", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testAlternatingIncreaseDecrease() {
        setScalarInput(0, 10.0);
        block.calculateOutput(0.0);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.1);
        assertEquals("Decrease detected", 1.0, getScalarOutput(0), DELTA);

        setScalarInput(0, 15.0);
        block.calculateOutput(0.2);
        assertEquals("Increase - no decrease", 0.0, getScalarOutput(0), DELTA);

        setScalarInput(0, 10.0);
        block.calculateOutput(0.3);
        assertEquals("Decrease detected again", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Zero Crossing Tests =====

    @Test
    public void testZeroCrossing_PositiveToZero() {
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);

        setScalarInput(0, 0.0);
        block.calculateOutput(0.1);
        assertEquals("Positive to zero is decrease", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testZeroCrossing_ZeroToNegative() {
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);

        setScalarInput(0, -5.0);
        block.calculateOutput(0.1);
        assertEquals("Zero to negative is decrease", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testZeroCrossing_PositiveToNegative() {
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);

        setScalarInput(0, -5.0);
        block.calculateOutput(0.1);
        assertEquals("Positive to negative is decrease", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testZeroCrossing_NegativeToZero() {
        setScalarInput(0, -5.0);
        block.calculateOutput(0.0);

        setScalarInput(0, 0.0);
        block.calculateOutput(0.1);
        assertEquals("Negative to zero is not decrease (increase)", 0.0, getScalarOutput(0), DELTA);
    }

    // ===== Edge Case Tests =====

    @Test
    public void testSmallDecrease() {
        setScalarInput(0, 1.0);
        block.calculateOutput(0.0);

        setScalarInput(0, 1.0 - 1e-10);
        block.calculateOutput(0.1);
        assertEquals("Small decrease should be detected", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLargeDecrease() {
        setScalarInput(0, 1e10);
        block.calculateOutput(0.0);

        setScalarInput(0, 0.0);
        block.calculateOutput(0.1);
        assertEquals("Large decrease should be detected", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testVerySmallDecrease_Precision() {
        setScalarInput(0, 1.0);
        block.calculateOutput(0.0);

        // Decrease by smallest distinguishable amount
        setScalarInput(0, 1.0 - Math.ulp(1.0));
        block.calculateOutput(0.1);
        assertEquals("Smallest distinguishable decrease", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNegativeValueDecrease() {
        setScalarInput(0, -5.0);
        block.calculateOutput(0.0);

        setScalarInput(0, -10.0);
        block.calculateOutput(0.1);
        assertEquals("Decrease from -5 to -10", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Custom Output Values Tests =====

    @Test
    public void testCustomOutputValues() {
        // VinWhenRising=2.0, VinWhenFalling=5.0
        block = DetectDecrease.create("testCustom", "test", "2.0", "5.0", -1.0,
            "Inherit: Logical (see Configuration Parameters: Optimization)", false, "0", mockModel);
        initializeOutputSignals(block);
        block.calculateInit(); // Initialize the block to set previousData

        setScalarInput(0, 10.0);
        block.calculateOutput(0.0);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.1);
        assertEquals("Custom falling value", 5.0, getScalarOutput(0), DELTA);

        setScalarInput(0, 15.0);
        block.calculateOutput(0.2);
        assertEquals("Custom rising value", 2.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNegativeOutputValues() {
        // VinWhenRising=-2.0, VinWhenFalling=-1.0
        block = DetectDecrease.create("testNegative", "test", "-2.0", "-1.0", -1.0,
            "Inherit: Logical (see Configuration Parameters: Optimization)", false, "0", mockModel);
        initializeOutputSignals(block);
        block.calculateInit(); // Initialize the block to set previousData

        setScalarInput(0, 10.0);
        block.calculateOutput(0.0);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.1);
        assertEquals("Negative falling value", -1.0, getScalarOutput(0), DELTA);
    }

    // ===== State Reset Tests (calculateInit) =====

    @Test
    public void testStateResetAfterCalculateInit() {
        // Build up state
        setScalarInput(0, 10.0);
        block.calculateOutput(0.0);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.1);
        assertEquals("Decrease detected", 1.0, getScalarOutput(0), DELTA);

        // Reset state - previousData is set to initialState (0.0)
        block.calculateInit();

        // After reset, 3.0 > initialState(0.0) = no decrease
        setScalarInput(0, 3.0);
        block.calculateOutput(0.2);
        assertEquals("After reset - 3.0 > initialState(0.0) = no decrease", 0.0, getScalarOutput(0), DELTA);

        // Second call after reset should detect decrease
        setScalarInput(0, 1.0);
        block.calculateOutput(0.3);
        assertEquals("After reset - second call detects decrease", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testInitialStateParameter() {
        // InitialState=10.0
        block = DetectDecrease.create("testInitial", "test", "0.0", "1.0", -1.0,
            "Inherit: Logical (see Configuration Parameters: Optimization)", false, "10.0", mockModel);
        initializeOutputSignals(block);

        // First call with value < initialState
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);
        // On first call, previousData is set to initialState (10.0)
        // So 5 < 10 should detect decrease
        assertEquals("First call with value < initialState", 0.0, getScalarOutput(0), DELTA);

        // Second call
        setScalarInput(0, 2.0);
        block.calculateOutput(0.1);
        assertEquals("Second call - decrease from 5 to 2", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Matrix Input Tests =====

    @Test
    public void testMatrixInput_Decrease() {
        Matrix initial = new Matrix(new double[][]{{5.0, 6.0}, {7.0, 8.0}});
        Matrix decreased = new Matrix(new double[][]{{4.0, 5.0}, {6.0, 7.0}});

        setMatrixInput(0, initial);
        block.calculateOutput(0.0);
        Matrix result1 = getMatrixOutput(0);
        // First call - after init, previousData is scalar 0.0
        // All elements compared against initialState (0.0), all > 0 so no decrease (risingValue = 0.0)
        assertEquals("First call element [0,0] - 5.0 > 0.0 = no decrease", 0.0, result1.get(0, 0), DELTA);
        assertEquals("First call element [1,1] - 8.0 > 0.0 = no decrease", 0.0, result1.get(1, 1), DELTA);

        setMatrixInput(0, decreased);
        block.calculateOutput(0.1);
        Matrix result2 = getMatrixOutput(0);
        // All elements decreased
        assertEquals("Decreased element [0,0]", 1.0, result2.get(0, 0), DELTA);
        assertEquals("Decreased element [0,1]", 1.0, result2.get(0, 1), DELTA);
        assertEquals("Decreased element [1,0]", 1.0, result2.get(1, 0), DELTA);
        assertEquals("Decreased element [1,1]", 1.0, result2.get(1, 1), DELTA);
    }

    @Test
    public void testMatrixInput_MixedChanges() {
        // Start with positive values
        Matrix initial = new Matrix(new double[][]{{-5.0, 0.0}, {5.0, 10.0}});
        setMatrixInput(0, initial);
        block.calculateOutput(0.0);
        Matrix result1 = getMatrixOutput(0);
        // First call compares to initialState (0.0)
        assertEquals("[0,0] -5.0 < 0.0 = decrease", 1.0, result1.get(0, 0), DELTA);
        assertEquals("[0,1] 0.0 == 0.0 = no decrease", 0.0, result1.get(0, 1), DELTA);
        assertEquals("[1,0] 5.0 > 0.0 = no decrease", 0.0, result1.get(1, 0), DELTA);
        assertEquals("[1,1] 10.0 > 0.0 = no decrease", 0.0, result1.get(1, 1), DELTA);

        // Now test mixed changes from initial values
        Matrix mixed = new Matrix(new double[][]{{-10.0, 0.0}, {10.0, 5.0}});
        setMatrixInput(0, mixed);
        block.calculateOutput(0.1);
        Matrix result2 = getMatrixOutput(0);

        assertEquals("[0,0] decreased: -5->-10", 1.0, result2.get(0, 0), DELTA);
        assertEquals("[0,1] no change: 0->0", 0.0, result2.get(0, 1), DELTA);
        assertEquals("[1,0] increased: 5->10", 0.0, result2.get(1, 0), DELTA);
        assertEquals("[1,1] decreased: 10->5", 1.0, result2.get(1, 1), DELTA);
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
        assertEquals("Block type should be DetectDecrease", "DetectDecrease", block.getBlockType());
    }

    @Test
    public void testPerformance() {
        setScalarInput(0, 5.0);
        long avgTime = measureCalculateOutputPerformance(10000);
        assertTrue("calculateOutput should complete in reasonable time", avgTime < 100000); // 100 microseconds
        System.out.println("DetectDecrease performance: " + (avgTime / 1000.0) + " microseconds");
    }
}
