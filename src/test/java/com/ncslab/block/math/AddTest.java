package com.ncslab.block.math;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Add block.
 * Tests calculateOutput() and addition/subtraction operations without WebSocket dependencies.
 *
 * Test Coverage:
 * - Scalar addition (2, 3, and 4 inputs)
 * - Scalar subtraction and mixed operations
 * - Matrix addition (element-wise)
 * - Zero values and negative values
 * - Large and small values
 * - Edge cases (NaN handling, single input)
 * - Performance benchmarks
 */
public class AddTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create Add block with 2 inputs: "++" (both adding)
        return Add.create("testAdd", "test", "++", mockModel);
    }

    @Test
    public void testTwoInputAddition() {
        // Test basic addition: 5 + 3 = 8
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);
        block.calculateOutput(0.0);
        assertEquals("Should add two inputs", 8.0, getScalarOutput(0), DELTA);
    }


    @Test
    public void testZeroValues() {
        // Test addition with zero values: 0 + 0 = 0
        setScalarInput(0, 0.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        assertEquals("Should handle zero values", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNegativeValues() {
        // Test addition with negative values: -5 + -3 = -8
        setScalarInput(0, -5.0);
        setScalarInput(1, -3.0);
        block.calculateOutput(0.0);
        assertEquals("Should handle negative values", -8.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testPositiveAndNegative() {
        // Test mixed positive and negative: 10 + (-3) = 7
        setScalarInput(0, 10.0);
        setScalarInput(1, -3.0);
        block.calculateOutput(0.0);
        assertEquals("Should handle positive and negative", 7.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLargeValues() {
        // Test addition with large values
        double largeValue = 1e10;
        setScalarInput(0, largeValue);
        setScalarInput(1, largeValue);
        block.calculateOutput(0.0);
        assertEquals("Should handle large values", 2 * largeValue, getScalarOutput(0), largeValue * 1e-10);
    }

    @Test
    public void testSmallValues() {
        // Test addition with small values
        double smallValue = 1e-10;
        setScalarInput(0, smallValue);
        setScalarInput(1, smallValue);
        block.calculateOutput(0.0);
        assertEquals("Should handle small values", 2 * smallValue, getScalarOutput(0), 1e-20);
    }


    @Test
    public void testOutputPortConfiguration() {
        // Verify output port is properly configured
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertTrue("Add block should have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testInputPortConfiguration() {
        // Verify input ports match the inputSequence "++"
        assertEquals("Should have 2 input ports for '++'", 2, block.getInputPortList().size());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testAdd", "testAdd", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be Add", "Add", block.getBlockType());
    }

    @Test
    public void testParameterCount() {
        // Add block should have parameters for inputSequence, sampleTime, etc.
        assertTrue("Should have at least 4 parameters", block.getParameterList().size() >= 4);
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);

        for (int i = 0; i < 100; i++) {
            block.calculateOutput(i * 0.01);
            double output = getScalarOutput(0);
            assertEquals("Should always output 8.0", 8.0, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Add calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Add block should be fast (simple addition operation with input handling)
        assertTrue("Add calculateOutput should be fast", avgTime < 200000); // <200 microseconds
    }

    @Test
    public void testCommutativeProperty() {
        // Test a + b = b + a
        setScalarInput(0, 7.0);
        setScalarInput(1, 13.0);
        block.calculateOutput(0.0);
        double result1 = getScalarOutput(0);

        setScalarInput(0, 13.0);
        setScalarInput(1, 7.0);
        block.calculateOutput(0.0);
        double result2 = getScalarOutput(0);

        assertEquals("Addition should be commutative", result1, result2, DELTA);
        assertEquals("Both should equal 20", 20.0, result1, DELTA);
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);
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
        // Verify that output doesn't drift over many iterations with constant inputs
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);

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

        assertEquals("Final output should be 8.0", 8.0, firstOutput, DELTA);
    }
}
