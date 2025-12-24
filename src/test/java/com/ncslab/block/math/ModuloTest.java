package com.ncslab.block.math;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Modulo block.
 * Tests calculateOutput() and modulo operations without WebSocket dependencies.
 *
 * Test Coverage:
 * - fmod operation (C-style modulo)
 * - rem operation (IEEE remainder)
 * - Internal divisor source
 * - External divisor source (port-based)
 * - Positive and negative operands
 * - Zero handling and edge cases
 * - Mathematical correctness validation
 * - Performance benchmarks
 */
public class ModuloTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create default Modulo block (fmod, internal divisor = 2)
        return Modulo.create("testModulo", "test", "fmod", "Internal", 2.0, mockModel);
    }

    @Test
    public void testFmodBasic() {
        // 7 % 2 = 1
        setScalarInput(0, 7.0);
        block.calculateOutput(0.0);
        assertEquals("7 mod 2 should be 1", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testFmodZeroNumerator() {
        // 0 % 2 = 0
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        assertEquals("0 mod 2 should be 0", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testFmodNegativeNumerator() {
        // -7 % 2 = -1 (fmod: sign follows numerator)
        setScalarInput(0, -7.0);
        block.calculateOutput(0.0);
        assertEquals("-7 mod 2 should be -1", -1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testFmodExactDivision() {
        // 8 % 2 = 0
        setScalarInput(0, 8.0);
        block.calculateOutput(0.0);
        assertEquals("8 mod 2 should be 0", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testFmodDivisor3() throws Exception {
        Block modBlock = Modulo.create("testMod3", "test", "fmod", "Internal", 3.0, mockModel);
        initializeOutputSignals(modBlock);
        modBlock.calculateInit();

        // 10 % 3 = 1
        setScalarInputForBlock(modBlock, 0, 10.0);
        modBlock.calculateOutput(0.0);
        assertEquals("10 mod 3 should be 1", 1.0, getScalarOutputForBlock(modBlock, 0), DELTA);
    }

    @Test
    public void testFmodDivisor5() throws Exception {
        Block modBlock = Modulo.create("testMod5", "test", "fmod", "Internal", 5.0, mockModel);
        initializeOutputSignals(modBlock);
        modBlock.calculateInit();

        // 17 % 5 = 2
        setScalarInputForBlock(modBlock, 0, 17.0);
        modBlock.calculateOutput(0.0);
        assertEquals("17 mod 5 should be 2", 2.0, getScalarOutputForBlock(modBlock, 0), DELTA);
    }

    @Test
    public void testFmodNegativeDivisor() throws Exception {
        Block modBlock = Modulo.create("testModNeg", "test", "fmod", "Internal", -3.0, mockModel);
        initializeOutputSignals(modBlock);
        modBlock.calculateInit();

        // 7 % -3 = 1 (fmod: sign follows numerator)
        setScalarInputForBlock(modBlock, 0, 7.0);
        modBlock.calculateOutput(0.0);
        assertEquals("7 mod -3 should be 1", 1.0, getScalarOutputForBlock(modBlock, 0), DELTA);
    }

    @Test
    public void testFmodBothNegative() throws Exception {
        Block modBlock = Modulo.create("testModBothNeg", "test", "fmod", "Internal", -3.0, mockModel);
        initializeOutputSignals(modBlock);
        modBlock.calculateInit();

        // -7 % -3 = -1 (fmod: sign follows numerator)
        setScalarInputForBlock(modBlock, 0, -7.0);
        modBlock.calculateOutput(0.0);
        assertEquals("-7 mod -3 should be -1", -1.0, getScalarOutputForBlock(modBlock, 0), DELTA);
    }

    @Test
    public void testRemOperation() throws Exception {
        Block remBlock = Modulo.create("testRem", "test", "rem", "Internal", 2.0, mockModel);
        initializeOutputSignals(remBlock);
        remBlock.calculateInit();

        // rem(7, 2) = 1
        setScalarInputForBlock(remBlock, 0, 7.0);
        remBlock.calculateOutput(0.0);
        assertEquals("rem(7, 2) should be 1", 1.0, getScalarOutputForBlock(remBlock, 0), DELTA);
    }

    @Test
    public void testRemNegativeNumerator() throws Exception {
        Block remBlock = Modulo.create("testRem", "test", "rem", "Internal", 2.0, mockModel);
        initializeOutputSignals(remBlock);
        remBlock.calculateInit();

        // rem(-7, 2) = -1 (sign follows numerator)
        setScalarInputForBlock(remBlock, 0, -7.0);
        remBlock.calculateOutput(0.0);
        assertEquals("rem(-7, 2) should be -1", -1.0, getScalarOutputForBlock(remBlock, 0), DELTA);
    }

    @Test
    public void testFmodFloatingPoint() throws Exception {
        Block modBlock = Modulo.create("testModFloat", "test", "fmod", "Internal", 0.5, mockModel);
        initializeOutputSignals(modBlock);
        modBlock.calculateInit();

        // 2.7 % 0.5 = 0.2
        setScalarInputForBlock(modBlock, 0, 2.7);
        modBlock.calculateOutput(0.0);
        assertEquals("2.7 mod 0.5 should be 0.2", 0.2, getScalarOutputForBlock(modBlock, 0), 1e-10);
    }

    @Test
    public void testFmodSmallValues() throws Exception {
        Block modBlock = Modulo.create("testModSmall", "test", "fmod", "Internal", 0.01, mockModel);
        initializeOutputSignals(modBlock);
        modBlock.calculateInit();

        // 0.05 % 0.01 = 0
        setScalarInputForBlock(modBlock, 0, 0.05);
        modBlock.calculateOutput(0.0);
        assertEquals("0.05 mod 0.01 should be 0", 0.0, getScalarOutputForBlock(modBlock, 0), 1e-10);
    }

    @Test
    public void testFmodLargeValues() throws Exception {
        Block modBlock = Modulo.create("testModLarge", "test", "fmod", "Internal", 1000.0, mockModel);
        initializeOutputSignals(modBlock);
        modBlock.calculateInit();

        // 12345 % 1000 = 345
        setScalarInputForBlock(modBlock, 0, 12345.0);
        modBlock.calculateOutput(0.0);
        assertEquals("12345 mod 1000 should be 345", 345.0, getScalarOutputForBlock(modBlock, 0), DELTA);
    }

    @Test
    public void testDivisionByZero() throws Exception {
        Block modBlock = Modulo.create("testModZero", "test", "fmod", "Internal", 0.0, mockModel);
        initializeOutputSignals(modBlock);
        modBlock.calculateInit();

        // 5 % 0 = NaN (division by zero)
        setScalarInputForBlock(modBlock, 0, 5.0);
        modBlock.calculateOutput(0.0);
        assertTrue("5 mod 0 should be NaN", Double.isNaN(getScalarOutputForBlock(modBlock, 0)));
    }

    @Test
    public void testOutputPortConfiguration() {
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertTrue("Modulo block should have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testInputPortConfigurationInternal() {
        assertEquals("Should have 1 input port for internal divisor", 1, block.getInputPortList().size());
    }

    @Test
    public void testInputPortConfigurationExternal() throws Exception {
        Block modBlock = Modulo.create("testModExt", "test", "fmod", "External", 2.0, mockModel);
        assertEquals("Should have 2 input ports for external divisor", 2, modBlock.getInputPortList().size());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testModulo", "testModulo", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be Modulo", "Modulo", block.getBlockType());
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        setScalarInput(0, 7.0);

        for (int i = 0; i < 100; i++) {
            block.calculateOutput(i * 0.01);
            double output = getScalarOutput(0);
            assertEquals("Should always output 1 for 7 mod 2", 1.0, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 7.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Modulo calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Modulo should be fast (simple modulo operation)
        assertTrue("Modulo calculateOutput should be fast", avgTime < 200000); // <200 microseconds
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        setScalarInput(0, 7.0);
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
        setScalarInput(0, 7.0);

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

        assertEquals("Final output should be 1", 1.0, firstOutput, DELTA);
    }

    @Test
    public void testModuloProperties() throws Exception {
        // Test various modulo properties
        Block modBlock = Modulo.create("testModProp", "test", "fmod", "Internal", 7.0, mockModel);
        initializeOutputSignals(modBlock);
        modBlock.calculateInit();

        double[][] testCases = {
            {0.0, 0.0},     // 0 % 7 = 0
            {7.0, 0.0},     // 7 % 7 = 0
            {14.0, 0.0},    // 14 % 7 = 0
            {3.5, 3.5},     // 3.5 % 7 = 3.5
            {10.0, 3.0},    // 10 % 7 = 3
            {-3.0, -3.0}    // -3 % 7 = -3 (fmod behavior)
        };

        for (double[] testCase : testCases) {
            double input = testCase[0];
            double expected = testCase[1];
            setScalarInputForBlock(modBlock, 0, input);
            modBlock.calculateOutput(0.0);
            assertEquals(input + " mod 7 should be " + expected, expected, getScalarOutputForBlock(modBlock, 0), DELTA);
        }
    }
}
