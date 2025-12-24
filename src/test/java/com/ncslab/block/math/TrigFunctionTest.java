package com.ncslab.block.math;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for TrigFunction block.
 * Tests calculateOutput() and trigonometric operations without WebSocket dependencies.
 *
 * Test Coverage:
 * - sin, cos, tan operations at various angles
 * - asin, acos, atan operations
 * - atan2 two-argument operation
 * - Zero, positive, negative values
 * - Edge cases (pi/2, pi, special angles)
 * - Mathematical correctness validation
 * - Performance benchmarks
 */
public class TrigFunctionTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create default TrigFunction block (sin)
        return TrigFunction.create("testTrigFunction", "test", "sin", mockModel);
    }

    @Test
    public void testSinZero() {
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        assertEquals("sin(0) should be 0", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSinPiOverTwo() {
        // sin(π/2) = 1
        setScalarInput(0, Math.PI / 2);
        block.calculateOutput(0.0);
        assertEquals("sin(π/2) should be 1", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSinPi() {
        // sin(π) = 0
        setScalarInput(0, Math.PI);
        block.calculateOutput(0.0);
        assertEquals("sin(π) should be 0", 0.0, getScalarOutput(0), 1e-10);
    }

    @Test
    public void testSinNegative() {
        // sin(-π/2) = -1
        setScalarInput(0, -Math.PI / 2);
        block.calculateOutput(0.0);
        assertEquals("sin(-π/2) should be -1", -1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSin30Degrees() throws Exception {
        // sin(30°) = sin(π/6) = 0.5
        setScalarInput(0, Math.PI / 6);
        block.calculateOutput(0.0);
        assertEquals("sin(30°) should be 0.5", 0.5, getScalarOutput(0), DELTA);
    }

    @Test
    public void testCosFunction() throws Exception {
        // Create cos block
        Block cosBlock = TrigFunction.create("testCos", "test", "cos", mockModel);
        initializeOutputSignals(cosBlock);
        cosBlock.calculateInit();

        // cos(0) = 1
        setScalarInputForBlock(cosBlock, 0, 0.0);
        cosBlock.calculateOutput(0.0);
        assertEquals("cos(0) should be 1", 1.0, getScalarOutputForBlock(cosBlock, 0), DELTA);
    }

    @Test
    public void testCosPiOverTwo() throws Exception {
        Block cosBlock = TrigFunction.create("testCos", "test", "cos", mockModel);
        initializeOutputSignals(cosBlock);
        cosBlock.calculateInit();

        // cos(π/2) = 0
        setScalarInputForBlock(cosBlock, 0, Math.PI / 2);
        cosBlock.calculateOutput(0.0);
        assertEquals("cos(π/2) should be 0", 0.0, getScalarOutputForBlock(cosBlock, 0), 1e-10);
    }

    @Test
    public void testCosPi() throws Exception {
        Block cosBlock = TrigFunction.create("testCos", "test", "cos", mockModel);
        initializeOutputSignals(cosBlock);
        cosBlock.calculateInit();

        // cos(π) = -1
        setScalarInputForBlock(cosBlock, 0, Math.PI);
        cosBlock.calculateOutput(0.0);
        assertEquals("cos(π) should be -1", -1.0, getScalarOutputForBlock(cosBlock, 0), DELTA);
    }

    @Test
    public void testTanFunction() throws Exception {
        Block tanBlock = TrigFunction.create("testTan", "test", "tan", mockModel);
        initializeOutputSignals(tanBlock);
        tanBlock.calculateInit();

        // tan(0) = 0
        setScalarInputForBlock(tanBlock, 0, 0.0);
        tanBlock.calculateOutput(0.0);
        assertEquals("tan(0) should be 0", 0.0, getScalarOutputForBlock(tanBlock, 0), DELTA);
    }

    @Test
    public void testTanPiOverFour() throws Exception {
        Block tanBlock = TrigFunction.create("testTan", "test", "tan", mockModel);
        initializeOutputSignals(tanBlock);
        tanBlock.calculateInit();

        // tan(π/4) = 1
        setScalarInputForBlock(tanBlock, 0, Math.PI / 4);
        tanBlock.calculateOutput(0.0);
        assertEquals("tan(π/4) should be 1", 1.0, getScalarOutputForBlock(tanBlock, 0), DELTA);
    }

    @Test
    public void testAsinFunction() throws Exception {
        Block asinBlock = TrigFunction.create("testAsin", "test", "asin", mockModel);
        initializeOutputSignals(asinBlock);
        asinBlock.calculateInit();

        // asin(0) = 0
        setScalarInputForBlock(asinBlock, 0, 0.0);
        asinBlock.calculateOutput(0.0);
        assertEquals("asin(0) should be 0", 0.0, getScalarOutputForBlock(asinBlock, 0), DELTA);
    }

    @Test
    public void testAsinOne() throws Exception {
        Block asinBlock = TrigFunction.create("testAsin", "test", "asin", mockModel);
        initializeOutputSignals(asinBlock);
        asinBlock.calculateInit();

        // asin(1) = π/2
        setScalarInputForBlock(asinBlock, 0, 1.0);
        asinBlock.calculateOutput(0.0);
        assertEquals("asin(1) should be π/2", Math.PI / 2, getScalarOutputForBlock(asinBlock, 0), DELTA);
    }

    @Test
    public void testAsinHalf() throws Exception {
        Block asinBlock = TrigFunction.create("testAsin", "test", "asin", mockModel);
        initializeOutputSignals(asinBlock);
        asinBlock.calculateInit();

        // asin(0.5) = π/6 (30 degrees)
        setScalarInputForBlock(asinBlock, 0, 0.5);
        asinBlock.calculateOutput(0.0);
        assertEquals("asin(0.5) should be π/6", Math.PI / 6, getScalarOutputForBlock(asinBlock, 0), DELTA);
    }

    @Test
    public void testAcosFunction() throws Exception {
        Block acosBlock = TrigFunction.create("testAcos", "test", "acos", mockModel);
        initializeOutputSignals(acosBlock);
        acosBlock.calculateInit();

        // acos(1) = 0
        setScalarInputForBlock(acosBlock, 0, 1.0);
        acosBlock.calculateOutput(0.0);
        assertEquals("acos(1) should be 0", 0.0, getScalarOutputForBlock(acosBlock, 0), DELTA);
    }

    @Test
    public void testAcosZero() throws Exception {
        Block acosBlock = TrigFunction.create("testAcos", "test", "acos", mockModel);
        initializeOutputSignals(acosBlock);
        acosBlock.calculateInit();

        // acos(0) = π/2
        setScalarInputForBlock(acosBlock, 0, 0.0);
        acosBlock.calculateOutput(0.0);
        assertEquals("acos(0) should be π/2", Math.PI / 2, getScalarOutputForBlock(acosBlock, 0), DELTA);
    }

    @Test
    public void testAtanFunction() throws Exception {
        Block atanBlock = TrigFunction.create("testAtan", "test", "atan", mockModel);
        initializeOutputSignals(atanBlock);
        atanBlock.calculateInit();

        // atan(0) = 0
        setScalarInputForBlock(atanBlock, 0, 0.0);
        atanBlock.calculateOutput(0.0);
        assertEquals("atan(0) should be 0", 0.0, getScalarOutputForBlock(atanBlock, 0), DELTA);
    }

    @Test
    public void testAtanOne() throws Exception {
        Block atanBlock = TrigFunction.create("testAtan", "test", "atan", mockModel);
        initializeOutputSignals(atanBlock);
        atanBlock.calculateInit();

        // atan(1) = π/4
        setScalarInputForBlock(atanBlock, 0, 1.0);
        atanBlock.calculateOutput(0.0);
        assertEquals("atan(1) should be π/4", Math.PI / 4, getScalarOutputForBlock(atanBlock, 0), DELTA);
    }

    @Test
    public void testAtan2Function() throws Exception {
        Block atan2Block = TrigFunction.create("testAtan2", "test", "atan2", mockModel);
        initializeOutputSignals(atan2Block);
        atan2Block.calculateInit();

        // atan2(1, 1) = π/4 (45 degrees)
        setScalarInputForBlock(atan2Block, 0, 1.0);  // y
        setScalarInputForBlock(atan2Block, 1, 1.0);  // x
        atan2Block.calculateOutput(0.0);
        assertEquals("atan2(1, 1) should be π/4", Math.PI / 4, getScalarOutputForBlock(atan2Block, 0), DELTA);
    }

    @Test
    public void testAtan2Quadrant2() throws Exception {
        Block atan2Block = TrigFunction.create("testAtan2", "test", "atan2", mockModel);
        initializeOutputSignals(atan2Block);
        atan2Block.calculateInit();

        // atan2(1, -1) = 3π/4 (135 degrees - Quadrant 2)
        setScalarInputForBlock(atan2Block, 0, 1.0);  // y
        setScalarInputForBlock(atan2Block, 1, -1.0); // x
        atan2Block.calculateOutput(0.0);
        assertEquals("atan2(1, -1) should be 3π/4", 3 * Math.PI / 4, getScalarOutputForBlock(atan2Block, 0), DELTA);
    }

    @Test
    public void testAtan2Quadrant3() throws Exception {
        Block atan2Block = TrigFunction.create("testAtan2", "test", "atan2", mockModel);
        initializeOutputSignals(atan2Block);
        atan2Block.calculateInit();

        // atan2(-1, -1) = -3π/4 (Quadrant 3)
        setScalarInputForBlock(atan2Block, 0, -1.0); // y
        setScalarInputForBlock(atan2Block, 1, -1.0); // x
        atan2Block.calculateOutput(0.0);
        assertEquals("atan2(-1, -1) should be -3π/4", -3 * Math.PI / 4, getScalarOutputForBlock(atan2Block, 0), DELTA);
    }

    @Test
    public void testSinLargeAngle() {
        // Test sin with large angle (wraps around)
        setScalarInput(0, 2 * Math.PI);
        block.calculateOutput(0.0);
        assertEquals("sin(2π) should be 0", 0.0, getScalarOutput(0), 1e-10);
    }

    @Test
    public void testSinSmallValue() {
        // For small x, sin(x) ≈ x
        double smallAngle = 0.001;
        setScalarInput(0, smallAngle);
        block.calculateOutput(0.0);
        assertEquals("sin(small x) ≈ x", smallAngle, getScalarOutput(0), 1e-6);
    }

    @Test
    public void testOutputPortConfiguration() {
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertTrue("TrigFunction block should have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testInputPortConfiguration() {
        assertEquals("Should have 1 input port for sin", 1, block.getInputPortList().size());
    }

    @Test
    public void testAtan2HasTwoInputs() throws Exception {
        Block atan2Block = TrigFunction.create("testAtan2", "test", "atan2", mockModel);
        assertEquals("atan2 should have 2 input ports", 2, atan2Block.getInputPortList().size());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testTrigFunction", "testTrigFunction", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be TrigFunction", "TrigFunction", block.getBlockType());
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        setScalarInput(0, Math.PI / 6);

        for (int i = 0; i < 100; i++) {
            block.calculateOutput(i * 0.01);
            double output = getScalarOutput(0);
            assertEquals("Should always output 0.5 for sin(π/6)", 0.5, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, Math.PI / 4);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average TrigFunction calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // TrigFunction should be fast (simple trig operation)
        assertTrue("TrigFunction calculateOutput should be fast", avgTime < 200000); // <200 microseconds
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        setScalarInput(0, Math.PI / 2);
        block.calculateOutput(0.0);
        double firstOutput = getScalarOutput(0);

        // Reinitialize
        block.calculateInit();
        block.calculateOutput(0.0);
        double secondOutput = getScalarOutput(0);

        assertEquals("Output should be same after reinitialization", firstOutput, secondOutput, DELTA);
    }

    @Test
    public void testNoDriftOverTime() {
        // Verify that output doesn't drift over many iterations with constant input
        setScalarInput(0, Math.PI / 6);

        double firstOutput = 0;
        for (int i = 0; i < 1000; i++) {
            block.calculateOutput(i * 0.001);
            double currentOutput = getScalarOutput(0);

            if (i == 0) {
                firstOutput = currentOutput;
            } else {
                assertEquals("Output should not drift at iteration " + i,
                    firstOutput, currentOutput, DELTA);
            }
        }

        assertEquals("Final output should be 0.5", 0.5, firstOutput, DELTA);
    }

    @Test
    public void testSinCosRelationship() throws Exception {
        // Test that sin²(x) + cos²(x) = 1
        Block cosBlock = TrigFunction.create("testCos", "test", "cos", mockModel);
        initializeOutputSignals(cosBlock);
        cosBlock.calculateInit();

        double angle = Math.PI / 3;

        setScalarInput(0, angle);
        block.calculateOutput(0.0);
        double sinValue = getScalarOutput(0);

        setScalarInputForBlock(cosBlock, 0, angle);
        cosBlock.calculateOutput(0.0);
        double cosValue = getScalarOutputForBlock(cosBlock, 0);

        double sumOfSquares = sinValue * sinValue + cosValue * cosValue;
        assertEquals("sin²(x) + cos²(x) should equal 1", 1.0, sumOfSquares, DELTA);
    }
}
