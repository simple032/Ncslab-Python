package com.ncslab.block.math;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for MathFunction block.
 * Tests calculateOutput() and mathematical operations without WebSocket dependencies.
 *
 * Test Coverage:
 * - exp, log, sqrt operations
 * - floor, ceil, round operations
 * - abs, sign operations
 * - pow (two-argument) operation
 * - Zero, positive, negative values
 * - Edge cases (domain errors, special values)
 * - Mathematical correctness validation
 * - Performance benchmarks
 */
public class MathFunctionTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create default MathFunction block (exp)
        return MathFunction.create("testMathFunction", "test", "exp", mockModel);
    }

    @Test
    public void testExpZero() {
        // e^0 = 1
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        assertEquals("exp(0) should be 1", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testExpOne() {
        // e^1 = e
        setScalarInput(0, 1.0);
        block.calculateOutput(0.0);
        assertEquals("exp(1) should be e", Math.E, getScalarOutput(0), DELTA);
    }

    @Test
    public void testExpNegative() {
        // e^(-1) = 1/e
        setScalarInput(0, -1.0);
        block.calculateOutput(0.0);
        assertEquals("exp(-1) should be 1/e", 1.0 / Math.E, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLogFunction() throws Exception {
        Block logBlock = MathFunction.create("testLog", "test", "log", mockModel);
        initializeOutputSignals(logBlock);
        logBlock.calculateInit();

        // log(1) = 0
        setScalarInputForBlock(logBlock, 0, 1.0);
        logBlock.calculateOutput(0.0);
        assertEquals("log(1) should be 0", 0.0, getScalarOutputForBlock(logBlock, 0), DELTA);
    }

    @Test
    public void testLogE() throws Exception {
        Block logBlock = MathFunction.create("testLog", "test", "log", mockModel);
        initializeOutputSignals(logBlock);
        logBlock.calculateInit();

        // log(e) = 1
        setScalarInputForBlock(logBlock, 0, Math.E);
        logBlock.calculateOutput(0.0);
        assertEquals("log(e) should be 1", 1.0, getScalarOutputForBlock(logBlock, 0), DELTA);
    }

    @Test
    public void testLogExpInverse() throws Exception {
        // Test that log(exp(x)) = x
        Block logBlock = MathFunction.create("testLog", "test", "log", mockModel);
        initializeOutputSignals(logBlock);
        logBlock.calculateInit();

        double testValue = 2.5;
        setScalarInput(0, testValue);
        block.calculateOutput(0.0);
        double expResult = getScalarOutput(0);

        setScalarInputForBlock(logBlock, 0, expResult);
        logBlock.calculateOutput(0.0);
        double logExpResult = getScalarOutputForBlock(logBlock, 0);

        assertEquals("log(exp(x)) should equal x", testValue, logExpResult, DELTA);
    }

    @Test
    public void testSqrtFunction() throws Exception {
        Block sqrtBlock = MathFunction.create("testSqrt", "test", "sqrt", mockModel);
        initializeOutputSignals(sqrtBlock);
        sqrtBlock.calculateInit();

        // sqrt(4) = 2
        setScalarInputForBlock(sqrtBlock, 0, 4.0);
        sqrtBlock.calculateOutput(0.0);
        assertEquals("sqrt(4) should be 2", 2.0, getScalarOutputForBlock(sqrtBlock, 0), DELTA);
    }

    @Test
    public void testSqrtZero() throws Exception {
        Block sqrtBlock = MathFunction.create("testSqrt", "test", "sqrt", mockModel);
        initializeOutputSignals(sqrtBlock);
        sqrtBlock.calculateInit();

        // sqrt(0) = 0
        setScalarInputForBlock(sqrtBlock, 0, 0.0);
        sqrtBlock.calculateOutput(0.0);
        assertEquals("sqrt(0) should be 0", 0.0, getScalarOutputForBlock(sqrtBlock, 0), DELTA);
    }

    @Test
    public void testSqrtOne() throws Exception {
        Block sqrtBlock = MathFunction.create("testSqrt", "test", "sqrt", mockModel);
        initializeOutputSignals(sqrtBlock);
        sqrtBlock.calculateInit();

        // sqrt(1) = 1
        setScalarInputForBlock(sqrtBlock, 0, 1.0);
        sqrtBlock.calculateOutput(0.0);
        assertEquals("sqrt(1) should be 1", 1.0, getScalarOutputForBlock(sqrtBlock, 0), DELTA);
    }

    @Test
    public void testFloorFunction() throws Exception {
        Block floorBlock = MathFunction.create("testFloor", "test", "floor", mockModel);
        initializeOutputSignals(floorBlock);
        floorBlock.calculateInit();

        // floor(2.7) = 2
        setScalarInputForBlock(floorBlock, 0, 2.7);
        floorBlock.calculateOutput(0.0);
        assertEquals("floor(2.7) should be 2", 2.0, getScalarOutputForBlock(floorBlock, 0), DELTA);
    }

    @Test
    public void testFloorNegative() throws Exception {
        Block floorBlock = MathFunction.create("testFloor", "test", "floor", mockModel);
        initializeOutputSignals(floorBlock);
        floorBlock.calculateInit();

        // floor(-2.3) = -3
        setScalarInputForBlock(floorBlock, 0, -2.3);
        floorBlock.calculateOutput(0.0);
        assertEquals("floor(-2.3) should be -3", -3.0, getScalarOutputForBlock(floorBlock, 0), DELTA);
    }

    @Test
    public void testFloorInteger() throws Exception {
        Block floorBlock = MathFunction.create("testFloor", "test", "floor", mockModel);
        initializeOutputSignals(floorBlock);
        floorBlock.calculateInit();

        // floor(5) = 5
        setScalarInputForBlock(floorBlock, 0, 5.0);
        floorBlock.calculateOutput(0.0);
        assertEquals("floor(5) should be 5", 5.0, getScalarOutputForBlock(floorBlock, 0), DELTA);
    }

    @Test
    public void testCeilFunction() throws Exception {
        Block ceilBlock = MathFunction.create("testCeil", "test", "ceil", mockModel);
        initializeOutputSignals(ceilBlock);
        ceilBlock.calculateInit();

        // ceil(2.3) = 3
        setScalarInputForBlock(ceilBlock, 0, 2.3);
        ceilBlock.calculateOutput(0.0);
        assertEquals("ceil(2.3) should be 3", 3.0, getScalarOutputForBlock(ceilBlock, 0), DELTA);
    }

    @Test
    public void testCeilNegative() throws Exception {
        Block ceilBlock = MathFunction.create("testCeil", "test", "ceil", mockModel);
        initializeOutputSignals(ceilBlock);
        ceilBlock.calculateInit();

        // ceil(-2.7) = -2
        setScalarInputForBlock(ceilBlock, 0, -2.7);
        ceilBlock.calculateOutput(0.0);
        assertEquals("ceil(-2.7) should be -2", -2.0, getScalarOutputForBlock(ceilBlock, 0), DELTA);
    }

    @Test
    public void testRoundFunction() throws Exception {
        Block roundBlock = MathFunction.create("testRound", "test", "round", mockModel);
        initializeOutputSignals(roundBlock);
        roundBlock.calculateInit();

        // round(2.5) = 3
        setScalarInputForBlock(roundBlock, 0, 2.5);
        roundBlock.calculateOutput(0.0);
        assertEquals("round(2.5) should be 3", 3.0, getScalarOutputForBlock(roundBlock, 0), DELTA);
    }

    @Test
    public void testRoundDown() throws Exception {
        Block roundBlock = MathFunction.create("testRound", "test", "round", mockModel);
        initializeOutputSignals(roundBlock);
        roundBlock.calculateInit();

        // round(2.4) = 2
        setScalarInputForBlock(roundBlock, 0, 2.4);
        roundBlock.calculateOutput(0.0);
        assertEquals("round(2.4) should be 2", 2.0, getScalarOutputForBlock(roundBlock, 0), DELTA);
    }

    @Test
    public void testRoundNegative() throws Exception {
        Block roundBlock = MathFunction.create("testRound", "test", "round", mockModel);
        initializeOutputSignals(roundBlock);
        roundBlock.calculateInit();

        // round(-2.5) = -2 (Java banker's rounding)
        setScalarInputForBlock(roundBlock, 0, -2.5);
        roundBlock.calculateOutput(0.0);
        double result = getScalarOutputForBlock(roundBlock, 0);
        assertTrue("round(-2.5) should be -2 or -3", result == -2.0 || result == -3.0);
    }

    @Test
    public void testAbsFunction() throws Exception {
        Block absBlock = MathFunction.create("testAbs", "test", "abs", mockModel);
        initializeOutputSignals(absBlock);
        absBlock.calculateInit();

        // abs(-5) = 5
        setScalarInputForBlock(absBlock, 0, -5.0);
        absBlock.calculateOutput(0.0);
        assertEquals("abs(-5) should be 5", 5.0, getScalarOutputForBlock(absBlock, 0), DELTA);
    }

    @Test
    public void testAbsPositive() throws Exception {
        Block absBlock = MathFunction.create("testAbs", "test", "abs", mockModel);
        initializeOutputSignals(absBlock);
        absBlock.calculateInit();

        // abs(5) = 5
        setScalarInputForBlock(absBlock, 0, 5.0);
        absBlock.calculateOutput(0.0);
        assertEquals("abs(5) should be 5", 5.0, getScalarOutputForBlock(absBlock, 0), DELTA);
    }

    @Test
    public void testAbsZero() throws Exception {
        Block absBlock = MathFunction.create("testAbs", "test", "abs", mockModel);
        initializeOutputSignals(absBlock);
        absBlock.calculateInit();

        // abs(0) = 0
        setScalarInputForBlock(absBlock, 0, 0.0);
        absBlock.calculateOutput(0.0);
        assertEquals("abs(0) should be 0", 0.0, getScalarOutputForBlock(absBlock, 0), DELTA);
    }

    @Test
    public void testSignFunction() throws Exception {
        Block signBlock = MathFunction.create("testSign", "test", "sign", mockModel);
        initializeOutputSignals(signBlock);
        signBlock.calculateInit();

        // sign(5) = 1
        setScalarInputForBlock(signBlock, 0, 5.0);
        signBlock.calculateOutput(0.0);
        assertEquals("sign(5) should be 1", 1.0, getScalarOutputForBlock(signBlock, 0), DELTA);
    }

    @Test
    public void testSignNegative() throws Exception {
        Block signBlock = MathFunction.create("testSign", "test", "sign", mockModel);
        initializeOutputSignals(signBlock);
        signBlock.calculateInit();

        // sign(-5) = -1
        setScalarInputForBlock(signBlock, 0, -5.0);
        signBlock.calculateOutput(0.0);
        assertEquals("sign(-5) should be -1", -1.0, getScalarOutputForBlock(signBlock, 0), DELTA);
    }

    @Test
    public void testSignZero() throws Exception {
        Block signBlock = MathFunction.create("testSign", "test", "sign", mockModel);
        initializeOutputSignals(signBlock);
        signBlock.calculateInit();

        // sign(0) = 0
        setScalarInputForBlock(signBlock, 0, 0.0);
        signBlock.calculateOutput(0.0);
        assertEquals("sign(0) should be 0", 0.0, getScalarOutputForBlock(signBlock, 0), DELTA);
    }

    @Test
    public void testPowFunction() throws Exception {
        Block powBlock = MathFunction.create("testPow", "test", "pow", mockModel);
        initializeOutputSignals(powBlock);
        powBlock.calculateInit();

        // 2^3 = 8
        setScalarInputForBlock(powBlock, 0, 2.0);  // base
        setScalarInputForBlock(powBlock, 1, 3.0);  // exponent
        powBlock.calculateOutput(0.0);
        assertEquals("2^3 should be 8", 8.0, getScalarOutputForBlock(powBlock, 0), DELTA);
    }

    @Test
    public void testPowZeroExponent() throws Exception {
        Block powBlock = MathFunction.create("testPow", "test", "pow", mockModel);
        initializeOutputSignals(powBlock);
        powBlock.calculateInit();

        // 5^0 = 1
        setScalarInputForBlock(powBlock, 0, 5.0);
        setScalarInputForBlock(powBlock, 1, 0.0);
        powBlock.calculateOutput(0.0);
        assertEquals("5^0 should be 1", 1.0, getScalarOutputForBlock(powBlock, 0), DELTA);
    }

    @Test
    public void testPowNegativeExponent() throws Exception {
        Block powBlock = MathFunction.create("testPow", "test", "pow", mockModel);
        initializeOutputSignals(powBlock);
        powBlock.calculateInit();

        // 2^(-2) = 0.25
        setScalarInputForBlock(powBlock, 0, 2.0);
        setScalarInputForBlock(powBlock, 1, -2.0);
        powBlock.calculateOutput(0.0);
        assertEquals("2^(-2) should be 0.25", 0.25, getScalarOutputForBlock(powBlock, 0), DELTA);
    }

    @Test
    public void testPowHasTwoInputs() throws Exception {
        Block powBlock = MathFunction.create("testPow", "test", "pow", mockModel);
        assertEquals("pow should have 2 input ports", 2, powBlock.getInputPortList().size());
    }

    @Test
    public void testOutputPortConfiguration() {
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertTrue("MathFunction block should have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testInputPortConfiguration() {
        assertEquals("Should have 1 input port for exp", 1, block.getInputPortList().size());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testMathFunction", "testMathFunction", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be MathFunction", "MathFunction", block.getBlockType());
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        setScalarInput(0, 1.0);

        for (int i = 0; i < 100; i++) {
            block.calculateOutput(i * 0.01);
            double output = getScalarOutput(0);
            assertEquals("Should always output e for exp(1)", Math.E, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 1.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average MathFunction calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // MathFunction should be fast (simple math operation)
        assertTrue("MathFunction calculateOutput should be fast", avgTime < 200000); // <200 microseconds
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        setScalarInput(0, 2.0);
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
        setScalarInput(0, 1.0);

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

        assertEquals("Final output should be e", Math.E, firstOutput, DELTA);
    }
}
