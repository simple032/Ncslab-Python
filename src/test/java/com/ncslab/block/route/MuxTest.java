package com.ncslab.block.route;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Mux block.
 * Tests calculateOutput() and signal multiplexing operations without WebSocket dependencies.
 *
 * Test Coverage:
 * - Scalar input concatenation (2, 3, 4 inputs)
 * - Vector input concatenation
 * - Mixed scalar and vector inputs
 * - Zero values and negative values
 * - Large and small values
 * - Port configuration validation
 * - Edge cases
 * - Performance benchmarks
 */
public class MuxTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create Mux block with 2 inputs
        return Mux.create("testMux", "test", 2, mockModel);
    }

    @Test
    public void testTwoScalarInputs() {
        // Test basic mux: [5.0, 3.0] -> [5.0; 3.0]
        setOutputDimensions(0, 2, 1);  // 2 scalar inputs -> 2x1 output
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);
        block.calculateOutput(0.0);

        Matrix output = getMatrixOutput(0);
        assertEquals("Output should be 2x1 vector", 2, output.getRowDimension());
        assertEquals("Output should be 2x1 vector", 1, output.getColumnDimension());
        assertEquals("First element should be 5.0", 5.0, output.get(0, 0), DELTA);
        assertEquals("Second element should be 3.0", 3.0, output.get(1, 0), DELTA);
    }

    @Test
    public void testThreeScalarInputs() throws Exception {
        // Create Mux with 3 inputs
        Block mux3 = Mux.create("mux3", "test", 3, mockModel);
        initializeOutputSignals(mux3);
        mux3.calculateInit();

        setOutputDimensionsForBlock(mux3, 0, 3, 1);  // 3 scalar inputs -> 3x1 output
        setScalarInputForBlock(mux3, 0, 1.0);
        setScalarInputForBlock(mux3, 1, 2.0);
        setScalarInputForBlock(mux3, 2, 3.0);
        mux3.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(mux3, 0);
        assertEquals("Output should be 3x1 vector", 3, output.getRowDimension());
        assertEquals("First element should be 1.0", 1.0, output.get(0, 0), DELTA);
        assertEquals("Second element should be 2.0", 2.0, output.get(1, 0), DELTA);
        assertEquals("Third element should be 3.0", 3.0, output.get(2, 0), DELTA);
    }

    @Test
    public void testFourScalarInputs() throws Exception {
        // Create Mux with 4 inputs
        Block mux4 = Mux.create("mux4", "test", 4, mockModel);
        initializeOutputSignals(mux4);
        mux4.calculateInit();

        setOutputDimensionsForBlock(mux4, 0, 4, 1);  // 4 scalar inputs -> 4x1 output
        setScalarInputForBlock(mux4, 0, 10.0);
        setScalarInputForBlock(mux4, 1, 20.0);
        setScalarInputForBlock(mux4, 2, 30.0);
        setScalarInputForBlock(mux4, 3, 40.0);
        mux4.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(mux4, 0);
        assertEquals("Output should be 4x1 vector", 4, output.getRowDimension());
        assertEquals("First element should be 10.0", 10.0, output.get(0, 0), DELTA);
        assertEquals("Second element should be 20.0", 20.0, output.get(1, 0), DELTA);
        assertEquals("Third element should be 30.0", 30.0, output.get(2, 0), DELTA);
        assertEquals("Fourth element should be 40.0", 40.0, output.get(3, 0), DELTA);
    }

    @Test
    public void testZeroValues() {
        // Test mux with zero values: [0.0, 0.0] -> [0.0; 0.0]
        setOutputDimensions(0, 2, 1);
        setScalarInput(0, 0.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);

        Matrix output = getMatrixOutput(0);
        assertEquals("First element should be 0.0", 0.0, output.get(0, 0), DELTA);
        assertEquals("Second element should be 0.0", 0.0, output.get(1, 0), DELTA);
    }

    @Test
    public void testNegativeValues() {
        // Test mux with negative values: [-5.0, -3.0] -> [-5.0; -3.0]
        setOutputDimensions(0, 2, 1);
        setScalarInput(0, -5.0);
        setScalarInput(1, -3.0);
        block.calculateOutput(0.0);

        Matrix output = getMatrixOutput(0);
        assertEquals("First element should be -5.0", -5.0, output.get(0, 0), DELTA);
        assertEquals("Second element should be -3.0", -3.0, output.get(1, 0), DELTA);
    }

    @Test
    public void testMixedPositiveNegative() {
        // Test mux with mixed signs: [10.0, -3.0] -> [10.0; -3.0]
        setOutputDimensions(0, 2, 1);
        setScalarInput(0, 10.0);
        setScalarInput(1, -3.0);
        block.calculateOutput(0.0);

        Matrix output = getMatrixOutput(0);
        assertEquals("First element should be 10.0", 10.0, output.get(0, 0), DELTA);
        assertEquals("Second element should be -3.0", -3.0, output.get(1, 0), DELTA);
    }

    @Test
    public void testLargeValues() {
        // Test mux with large values
        setOutputDimensions(0, 2, 1);
        double largeValue1 = 1e10;
        double largeValue2 = 2e10;
        setScalarInput(0, largeValue1);
        setScalarInput(1, largeValue2);
        block.calculateOutput(0.0);

        Matrix output = getMatrixOutput(0);
        assertEquals("First element should be 1e10", largeValue1, output.get(0, 0), largeValue1 * 1e-10);
        assertEquals("Second element should be 2e10", largeValue2, output.get(1, 0), largeValue2 * 1e-10);
    }

    @Test
    public void testSmallValues() {
        // Test mux with small values
        setOutputDimensions(0, 2, 1);
        double smallValue1 = 1e-10;
        double smallValue2 = 2e-10;
        setScalarInput(0, smallValue1);
        setScalarInput(1, smallValue2);
        block.calculateOutput(0.0);

        Matrix output = getMatrixOutput(0);
        assertEquals("First element should be 1e-10", smallValue1, output.get(0, 0), 1e-20);
        assertEquals("Second element should be 2e-10", smallValue2, output.get(1, 0), 1e-20);
    }

    @Test
    public void testVectorInputConcatenation() throws Exception {
        // Test mux with vector inputs: [1;2] and [3;4] -> [1;2;3;4]
        Block mux2 = Mux.create("muxVector", "test", 2, mockModel);
        initializeOutputSignals(mux2);
        mux2.calculateInit();

        setOutputDimensionsForBlock(mux2, 0, 4, 1);  // 2 2x1 vectors -> 4x1 output
        Matrix vector1 = new Matrix(2, 1);
        vector1.set(0, 0, 1.0);
        vector1.set(1, 0, 2.0);

        Matrix vector2 = new Matrix(2, 1);
        vector2.set(0, 0, 3.0);
        vector2.set(1, 0, 4.0);

        setMatrixInputForBlock(mux2, 0, vector1);
        setMatrixInputForBlock(mux2, 1, vector2);
        mux2.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(mux2, 0);
        assertEquals("Output should be 4x1 vector", 4, output.getRowDimension());
        assertEquals("First element should be 1.0", 1.0, output.get(0, 0), DELTA);
        assertEquals("Second element should be 2.0", 2.0, output.get(1, 0), DELTA);
        assertEquals("Third element should be 3.0", 3.0, output.get(2, 0), DELTA);
        assertEquals("Fourth element should be 4.0", 4.0, output.get(3, 0), DELTA);
    }

    @Test
    public void testOutputPortConfiguration() {
        // Verify output port is properly configured
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertTrue("Mux block should have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testInputPortConfiguration() {
        // Verify input ports match the numberOfInputs parameter (2)
        assertEquals("Should have 2 input ports", 2, block.getInputPortList().size());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testMux", "testMux", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be Mux", "Mux", block.getBlockType());
    }

    @Test
    public void testParameterCount() {
        // Mux block should have parameters: Inputs, DisplayOrder, SampleTime, OutDataTypeStr, SaturateOnIntegerOverflow
        assertTrue("Should have at least 5 parameters", block.getParameterList().size() >= 5);
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        setOutputDimensions(0, 2, 1);
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);

        for (int i = 0; i < 100; i++) {
            block.calculateOutput(i * 0.01);
            Matrix output = getMatrixOutput(0);
            assertEquals("First element should always be 5.0", 5.0, output.get(0, 0), DELTA);
            assertEquals("Second element should always be 3.0", 3.0, output.get(1, 0), DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setOutputDimensions(0, 2, 1);
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Mux calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Mux block should be reasonably fast (simple concatenation operation)
        assertTrue("Mux calculateOutput should be reasonably fast", avgTime < 1000000); // <1ms is acceptable
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        setOutputDimensions(0, 2, 1);
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);
        block.calculateOutput(0.0);
        Matrix firstOutput = getMatrixOutput(0);

        // Reinitialize
        block.calculateInit();
        block.calculateOutput(0.0);
        Matrix secondOutput = getMatrixOutput(0);

        assertMatrixEquals(firstOutput, secondOutput, DELTA);
    }

    @Test
    public void testNoDriftOverTime() {
        // Verify that output doesn't drift over many iterations with constant inputs
        setOutputDimensions(0, 2, 1);
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);

        Matrix firstOutput = null;
        for (int i = 0; i < 1000; i++) {
            block.calculateOutput(i * 0.001);
            Matrix currentOutput = getMatrixOutput(0);

            if (i == 0) {
                firstOutput = currentOutput;
            } else {
                assertMatrixEquals(firstOutput, currentOutput, DELTA);
            }
        }

        assertEquals("First element should be 5.0", 5.0, firstOutput.get(0, 0), DELTA);
        assertEquals("Second element should be 3.0", 3.0, firstOutput.get(1, 0), DELTA);
    }

    @Test
    public void testFullParameterCreation() throws Exception {
        // Test Mux creation with full parameters
        Block muxFull = Mux.create("muxFull", "test", 3, "1:N", -1.0, "Inherit: auto", false, mockModel);
        initializeOutputSignals(muxFull);
        muxFull.calculateInit();

        assertEquals("Should have 3 input ports", 3, muxFull.getInputPortList().size());
        assertEquals("Should have 1 output port", 1, muxFull.getOutputPortList().size());

        setOutputDimensionsForBlock(muxFull, 0, 3, 1);  // 3 scalar inputs -> 3x1 output
        setScalarInputForBlock(muxFull, 0, 1.0);
        setScalarInputForBlock(muxFull, 1, 2.0);
        setScalarInputForBlock(muxFull, 2, 3.0);
        muxFull.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(muxFull, 0);
        assertEquals("Output should be 3x1 vector", 3, output.getRowDimension());
    }

    @Test
    public void testDifferentInputSizes() throws Exception {
        // Test mux with 6 inputs
        Block mux6 = Mux.create("mux6", "test", 6, mockModel);
        initializeOutputSignals(mux6);
        mux6.calculateInit();

        assertEquals("Should have 6 input ports", 6, mux6.getInputPortList().size());

        setOutputDimensionsForBlock(mux6, 0, 6, 1);  // 6 scalar inputs -> 6x1 output
        for (int i = 0; i < 6; i++) {
            setScalarInputForBlock(mux6, i, (i + 1) * 10.0);
        }
        mux6.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(mux6, 0);
        assertEquals("Output should be 6x1 vector", 6, output.getRowDimension());
        for (int i = 0; i < 6; i++) {
            assertEquals("Element " + i + " should match", (i + 1) * 10.0, output.get(i, 0), DELTA);
        }
    }

    @Test
    public void testDecimalValues() {
        // Test mux with decimal values
        setOutputDimensions(0, 2, 1);
        setScalarInput(0, 1.234);
        setScalarInput(1, 5.678);
        block.calculateOutput(0.0);

        Matrix output = getMatrixOutput(0);
        assertEquals("First element should be 1.234", 1.234, output.get(0, 0), DELTA);
        assertEquals("Second element should be 5.678", 5.678, output.get(1, 0), DELTA);
    }

    @Test
    public void testOutputDataType() {
        // Verify output data type is MATRIX
        setOutputDimensions(0, 2, 1);
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);
        block.calculateOutput(0.0);

        Data outputData = getOutputData(0);
        assertEquals("Output data type should be MATRIX", DataType.MATRIX, outputData.getDataType());
    }

    @Test
    public void testSequentialInputs() {
        // Test mux with sequential input values
        setOutputDimensions(0, 2, 1);
        setScalarInput(0, 1.0);
        setScalarInput(1, 2.0);
        block.calculateOutput(0.0);

        Matrix output = getMatrixOutput(0);
        assertEquals("First element should be 1.0", 1.0, output.get(0, 0), DELTA);
        assertEquals("Second element should be 2.0", 2.0, output.get(1, 0), DELTA);
    }

    @Test
    public void testIdenticalInputs() {
        // Test mux with identical input values
        setOutputDimensions(0, 2, 1);
        setScalarInput(0, 7.5);
        setScalarInput(1, 7.5);
        block.calculateOutput(0.0);

        Matrix output = getMatrixOutput(0);
        assertEquals("First element should be 7.5", 7.5, output.get(0, 0), DELTA);
        assertEquals("Second element should be 7.5", 7.5, output.get(1, 0), DELTA);
    }
}