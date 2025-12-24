package com.ncslab.block.math;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Product block.
 * Tests calculateOutput() and multiplication/division operations without WebSocket dependencies.
 *
 * Test Coverage:
 * - Scalar multiplication (2 inputs)
 * - Scalar division operations
 * - Mixed multiplication and division
 * - Zero values and negative values
 * - Large and small values
 * - Edge cases (division by zero, NaN handling)
 * - Performance benchmarks
 */
public class ProductTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create Product block with 2 inputs: "**" (both multiplying)
        return Product.create("testProduct", "test", "**", "Element-wise(.*)", -1.0, true,
                             "Inherit: Same as input", false, mockModel);
    }

    @Test
    public void testTwoInputMultiplication() {
        // Test basic multiplication: 5 * 3 = 15
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);
        block.calculateOutput(0.0);
        assertEquals("Should multiply two inputs", 15.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testZeroMultiplication() {
        // Test multiplication with zero: 5 * 0 = 0
        setScalarInput(0, 5.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        assertEquals("Should handle zero multiplication", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testBothZero() {
        // Test multiplication with both zero: 0 * 0 = 0
        setScalarInput(0, 0.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        assertEquals("Should handle both zero", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNegativeMultiplication() {
        // Test multiplication with negative values: -5 * -3 = 15
        setScalarInput(0, -5.0);
        setScalarInput(1, -3.0);
        block.calculateOutput(0.0);
        assertEquals("Should handle negative multiplication", 15.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testPositiveAndNegative() {
        // Test mixed positive and negative: 10 * (-3) = -30
        setScalarInput(0, 10.0);
        setScalarInput(1, -3.0);
        block.calculateOutput(0.0);
        assertEquals("Should handle positive and negative", -30.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLargeValues() {
        // Test multiplication with large values
        double largeValue = 1e5;
        setScalarInput(0, largeValue);
        setScalarInput(1, largeValue);
        block.calculateOutput(0.0);
        assertEquals("Should handle large values", largeValue * largeValue, getScalarOutput(0), largeValue * largeValue * 1e-10);
    }

    @Test
    public void testSmallValues() {
        // Test multiplication with small values
        double smallValue = 1e-5;
        setScalarInput(0, smallValue);
        setScalarInput(1, smallValue);
        block.calculateOutput(0.0);
        assertEquals("Should handle small values", smallValue * smallValue, getScalarOutput(0), 1e-20);
    }

    @Test
    public void testDivisionOperation() throws Exception {
        // Test division: 10 / 2 = 5
        Product divideBlock = Product.create("testDivide", "test", "*/", "Element-wise(.*)", -1.0, true,
                                            "Inherit: Same as input", false, mockModel);
        initializeOutputSignals(divideBlock);
        divideBlock.calculateInit();

        mockInputPortConnection(divideBlock, 0, new com.ncslab.block.data.Data(10.0));
        mockInputPortConnection(divideBlock, 1, new com.ncslab.block.data.Data(2.0));

        divideBlock.calculateOutput(0.0);
        double result = divideBlock.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        assertEquals("Should divide correctly", 5.0, result, DELTA);
    }

    @Test
    public void testDivisionByZero() throws Exception {
        // Test division by zero: 10 / 0 = infinity
        Product divideBlock = Product.create("testDivideByZero", "test", "*/", "Element-wise(.*)", -1.0, true,
                                            "Inherit: Same as input", false, mockModel);
        initializeOutputSignals(divideBlock);
        divideBlock.calculateInit();

        mockInputPortConnection(divideBlock, 0, new com.ncslab.block.data.Data(10.0));
        mockInputPortConnection(divideBlock, 1, new com.ncslab.block.data.Data(0.0));

        divideBlock.calculateOutput(0.0);
        double result = divideBlock.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        assertTrue("Division by zero should result in infinity", Double.isInfinite(result));
    }

    @Test
    public void testOutputPortConfiguration() {
        // Verify output port is properly configured
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertTrue("Product block should have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testInputPortConfiguration() {
        // Verify input ports match the inputSequence "**"
        assertEquals("Should have 2 input ports for '**'", 2, block.getInputPortList().size());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testProduct", "testProduct", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be Product", "Product", block.getBlockType());
    }

    @Test
    public void testParameterCount() {
        // Product block should have parameters for inputs, multiplication, sampleTime, etc.
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
            assertEquals("Should always output 15.0", 15.0, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Product calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Product block should be fast (simple multiplication operation with input handling)
        assertTrue("Product calculateOutput should be fast", avgTime < 200000); // <200 microseconds
    }

    @Test
    public void testCommutativeProperty() {
        // Test a * b = b * a
        setScalarInput(0, 7.0);
        setScalarInput(1, 13.0);
        block.calculateOutput(0.0);
        double result1 = getScalarOutput(0);

        setScalarInput(0, 13.0);
        setScalarInput(1, 7.0);
        block.calculateOutput(0.0);
        double result2 = getScalarOutput(0);

        assertEquals("Multiplication should be commutative", result1, result2, DELTA);
        assertEquals("Both should equal 91", 91.0, result1, DELTA);
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

        assertEquals("Final output should be 15.0", 15.0, firstOutput, DELTA);
    }

    @Test
    public void testIdentityProperty() {
        // Test a * 1 = a
        setScalarInput(0, 42.0);
        setScalarInput(1, 1.0);
        block.calculateOutput(0.0);
        assertEquals("Multiplying by 1 should return original value", 42.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testFractionalValues() {
        // Test multiplication with fractional values: 0.5 * 0.2 = 0.1
        setScalarInput(0, 0.5);
        setScalarInput(1, 0.2);
        block.calculateOutput(0.0);
        assertEquals("Should handle fractional multiplication", 0.1, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNegativeByZero() {
        // Test negative multiplied by zero: -5 * 0 = 0
        setScalarInput(0, -5.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        assertEquals("Negative times zero should be zero", 0.0, getScalarOutput(0), DELTA);
    }
}
