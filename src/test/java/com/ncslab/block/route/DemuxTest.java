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
 * Direct Mockito-based tests for Demux block.
 * Tests calculateOutput() and signal demultiplexing operations without WebSocket dependencies.
 *
 * Test Coverage:
 * - Vector-to-scalar demultiplexing (2, 3, 4 outputs)
 * - Zero values and negative values
 * - Large and small values
 * - Mixed value types
 * - Port configuration validation
 * - Edge cases
 * - Performance benchmarks
 */
public class DemuxTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create Demux block with 2 outputs
        return Demux.create("testDemux", "test", 2, mockModel);
    }

    @Test
    public void testTwoScalarOutputs() {
        // Test basic demux: [5.0; 3.0] -> 5.0 and 3.0
        Matrix input = new Matrix(2, 1);
        input.set(0, 0, 5.0);
        input.set(1, 0, 3.0);

        setMatrixInput(0, input);
        block.calculateOutput(0.0);

        // Check that both outputs are scalar
        double output1 = getScalarOutputForBlock(block, 0);
        double output2 = getScalarOutputForBlock(block, 1);

        assertEquals("First output should be 5.0", 5.0, output1, DELTA);
        assertEquals("Second output should be 3.0", 3.0, output2, DELTA);
    }

    @Test
    public void testThreeScalarOutputs() throws Exception {
        // Create Demux with 3 outputs
        Block demux3 = Demux.create("demux3", "test", 3, mockModel);
        initializeOutputSignals(demux3);
        demux3.calculateInit();

        Matrix input = new Matrix(3, 1);
        input.set(0, 0, 1.0);
        input.set(1, 0, 2.0);
        input.set(2, 0, 3.0);

        setMatrixInputForBlock(demux3, 0, input);
        demux3.calculateOutput(0.0);

        assertEquals("First output should be 1.0", 1.0, getScalarOutputForBlock(demux3, 0), DELTA);
        assertEquals("Second output should be 2.0", 2.0, getScalarOutputForBlock(demux3, 1), DELTA);
        assertEquals("Third output should be 3.0", 3.0, getScalarOutputForBlock(demux3, 2), DELTA);
    }

    @Test
    public void testFourScalarOutputs() throws Exception {
        // Create Demux with 4 outputs
        Block demux4 = Demux.create("demux4", "test", 4, mockModel);
        initializeOutputSignals(demux4);
        demux4.calculateInit();

        Matrix input = new Matrix(4, 1);
        input.set(0, 0, 10.0);
        input.set(1, 0, 20.0);
        input.set(2, 0, 30.0);
        input.set(3, 0, 40.0);

        setMatrixInputForBlock(demux4, 0, input);
        demux4.calculateOutput(0.0);

        assertEquals("First output should be 10.0", 10.0, getScalarOutputForBlock(demux4, 0), DELTA);
        assertEquals("Second output should be 20.0", 20.0, getScalarOutputForBlock(demux4, 1), DELTA);
        assertEquals("Third output should be 30.0", 30.0, getScalarOutputForBlock(demux4, 2), DELTA);
        assertEquals("Fourth output should be 40.0", 40.0, getScalarOutputForBlock(demux4, 3), DELTA);
    }

    @Test
    public void testZeroValues() {
        // Test demux with zero values: [0.0; 0.0] -> 0.0 and 0.0
        Matrix input = new Matrix(2, 1);
        input.set(0, 0, 0.0);
        input.set(1, 0, 0.0);

        setMatrixInput(0, input);
        block.calculateOutput(0.0);

        assertEquals("First output should be 0.0", 0.0, getScalarOutputForBlock(block, 0), DELTA);
        assertEquals("Second output should be 0.0", 0.0, getScalarOutputForBlock(block, 1), DELTA);
    }

    @Test
    public void testNegativeValues() {
        // Test demux with negative values: [-5.0; -3.0] -> -5.0 and -3.0
        Matrix input = new Matrix(2, 1);
        input.set(0, 0, -5.0);
        input.set(1, 0, -3.0);

        setMatrixInput(0, input);
        block.calculateOutput(0.0);

        assertEquals("First output should be -5.0", -5.0, getScalarOutputForBlock(block, 0), DELTA);
        assertEquals("Second output should be -3.0", -3.0, getScalarOutputForBlock(block, 1), DELTA);
    }

    @Test
    public void testMixedPositiveNegative() {
        // Test demux with mixed signs: [10.0; -3.0] -> 10.0 and -3.0
        Matrix input = new Matrix(2, 1);
        input.set(0, 0, 10.0);
        input.set(1, 0, -3.0);

        setMatrixInput(0, input);
        block.calculateOutput(0.0);

        assertEquals("First output should be 10.0", 10.0, getScalarOutputForBlock(block, 0), DELTA);
        assertEquals("Second output should be -3.0", -3.0, getScalarOutputForBlock(block, 1), DELTA);
    }

    @Test
    public void testLargeValues() {
        // Test demux with large values
        double largeValue1 = 1e10;
        double largeValue2 = 2e10;

        Matrix input = new Matrix(2, 1);
        input.set(0, 0, largeValue1);
        input.set(1, 0, largeValue2);

        setMatrixInput(0, input);
        block.calculateOutput(0.0);

        assertEquals("First output should be 1e10", largeValue1, getScalarOutputForBlock(block, 0), largeValue1 * 1e-10);
        assertEquals("Second output should be 2e10", largeValue2, getScalarOutputForBlock(block, 1), largeValue2 * 1e-10);
    }

    @Test
    public void testSmallValues() {
        // Test demux with small values
        double smallValue1 = 1e-10;
        double smallValue2 = 2e-10;

        Matrix input = new Matrix(2, 1);
        input.set(0, 0, smallValue1);
        input.set(1, 0, smallValue2);

        setMatrixInput(0, input);
        block.calculateOutput(0.0);

        assertEquals("First output should be 1e-10", smallValue1, getScalarOutputForBlock(block, 0), 1e-20);
        assertEquals("Second output should be 2e-10", smallValue2, getScalarOutputForBlock(block, 1), 1e-20);
    }

    @Test
    public void testRowVectorInput() throws Exception {
        // Test demux with row vector input: [1, 2, 3] -> 1, 2, and 3
        Block demux3 = Demux.create("demuxRow", "test", 3, mockModel);
        initializeOutputSignals(demux3);
        demux3.calculateInit();

        Matrix input = new Matrix(1, 3);  // Row vector
        input.set(0, 0, 1.0);
        input.set(0, 1, 2.0);
        input.set(0, 2, 3.0);

        setMatrixInputForBlock(demux3, 0, input);
        demux3.calculateOutput(0.0);

        assertEquals("First output should be 1.0", 1.0, getScalarOutputForBlock(demux3, 0), DELTA);
        assertEquals("Second output should be 2.0", 2.0, getScalarOutputForBlock(demux3, 1), DELTA);
        assertEquals("Third output should be 3.0", 3.0, getScalarOutputForBlock(demux3, 2), DELTA);
    }

    @Test
    public void testOutputPortConfiguration() {
        // Verify output ports are properly configured (2 output ports)
        assertEquals("Should have 2 output ports", 2, block.getOutputPortList().size());

        OutputPort outputPort1 = block.getOutputPortList().get(0);
        OutputPort outputPort2 = block.getOutputPortList().get(1);

        assertNotNull("Output port 1 should exist", outputPort1);
        assertNotNull("Output port 2 should exist", outputPort2);
        assertTrue("Demux block should have feedthrough on output 1", outputPort1.getFeedThrough());
        assertTrue("Demux block should have feedthrough on output 2", outputPort2.getFeedThrough());
    }

    @Test
    public void testInputPortConfiguration() {
        // Verify input port is properly configured (single input port)
        assertEquals("Should have 1 input port", 1, block.getInputPortList().size());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testDemux", "testDemux", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be Demux", "Demux", block.getBlockType());
    }

    @Test
    public void testParameterCount() {
        // Demux block should have parameters: Outputs, DisplayOrder, SampleTime, OutDataTypeStr, SaturateOnIntegerOverflow
        assertTrue("Should have at least 5 parameters", block.getParameterList().size() >= 5);
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        Matrix input = new Matrix(2, 1);
        input.set(0, 0, 5.0);
        input.set(1, 0, 3.0);

        setMatrixInput(0, input);

        for (int i = 0; i < 100; i++) {
            block.calculateOutput(i * 0.01);
            assertEquals("First output should always be 5.0", 5.0, getScalarOutputForBlock(block, 0), DELTA);
            assertEquals("Second output should always be 3.0", 3.0, getScalarOutputForBlock(block, 1), DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        Matrix input = new Matrix(2, 1);
        input.set(0, 0, 5.0);
        input.set(1, 0, 3.0);

        setMatrixInput(0, input);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Demux calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Demux block should be reasonably fast (simple splitting operation)
        assertTrue("Demux calculateOutput should be reasonably fast", avgTime < 1000000); // <1ms is acceptable
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        Matrix input = new Matrix(2, 1);
        input.set(0, 0, 5.0);
        input.set(1, 0, 3.0);

        setMatrixInput(0, input);
        block.calculateOutput(0.0);
        double firstOutput1 = getScalarOutputForBlock(block, 0);
        double firstOutput2 = getScalarOutputForBlock(block, 1);

        // Reinitialize
        block.calculateInit();
        block.calculateOutput(0.0);
        double secondOutput1 = getScalarOutputForBlock(block, 0);
        double secondOutput2 = getScalarOutputForBlock(block, 1);

        assertEquals("First output should match after reinit", firstOutput1, secondOutput1, DELTA);
        assertEquals("Second output should match after reinit", firstOutput2, secondOutput2, DELTA);
    }

    @Test
    public void testNoDriftOverTime() {
        // Verify that output doesn't drift over many iterations with constant inputs
        Matrix input = new Matrix(2, 1);
        input.set(0, 0, 5.0);
        input.set(1, 0, 3.0);

        setMatrixInput(0, input);

        double firstOutput1 = 0.0, firstOutput2 = 0.0;
        for (int i = 0; i < 1000; i++) {
            block.calculateOutput(i * 0.001);
            double currentOutput1 = getScalarOutputForBlock(block, 0);
            double currentOutput2 = getScalarOutputForBlock(block, 1);

            if (i == 0) {
                firstOutput1 = currentOutput1;
                firstOutput2 = currentOutput2;
            } else {
                assertEquals("First output should not drift", firstOutput1, currentOutput1, DELTA);
                assertEquals("Second output should not drift", firstOutput2, currentOutput2, DELTA);
            }
        }

        assertEquals("First output should be 5.0", 5.0, firstOutput1, DELTA);
        assertEquals("Second output should be 3.0", 3.0, firstOutput2, DELTA);
    }

    @Test
    public void testFullParameterCreation() throws Exception {
        // Test Demux creation with full parameters
        Block demuxFull = Demux.create("demuxFull", "test", 3, "1:N", -1.0, "Inherit: auto", false, mockModel);
        initializeOutputSignals(demuxFull);
        demuxFull.calculateInit();

        assertEquals("Should have 1 input port", 1, demuxFull.getInputPortList().size());
        assertEquals("Should have 3 output ports", 3, demuxFull.getOutputPortList().size());

        Matrix input = new Matrix(3, 1);
        input.set(0, 0, 1.0);
        input.set(1, 0, 2.0);
        input.set(2, 0, 3.0);

        setMatrixInputForBlock(demuxFull, 0, input);
        demuxFull.calculateOutput(0.0);

        assertEquals("First output should be 1.0", 1.0, getScalarOutputForBlock(demuxFull, 0), DELTA);
        assertEquals("Second output should be 2.0", 2.0, getScalarOutputForBlock(demuxFull, 1), DELTA);
        assertEquals("Third output should be 3.0", 3.0, getScalarOutputForBlock(demuxFull, 2), DELTA);
    }

    @Test
    public void testDifferentOutputSizes() throws Exception {
        // Test demux with 6 outputs
        Block demux6 = Demux.create("demux6", "test", 6, mockModel);
        initializeOutputSignals(demux6);
        demux6.calculateInit();

        assertEquals("Should have 6 output ports", 6, demux6.getOutputPortList().size());

        Matrix input = new Matrix(6, 1);
        for (int i = 0; i < 6; i++) {
            input.set(i, 0, (i + 1) * 10.0);
        }

        setMatrixInputForBlock(demux6, 0, input);
        demux6.calculateOutput(0.0);

        for (int i = 0; i < 6; i++) {
            assertEquals("Output " + i + " should match", (i + 1) * 10.0, getScalarOutputForBlock(demux6, i), DELTA);
        }
    }

    @Test
    public void testDecimalValues() {
        // Test demux with decimal values
        Matrix input = new Matrix(2, 1);
        input.set(0, 0, 1.234);
        input.set(1, 0, 5.678);

        setMatrixInput(0, input);
        block.calculateOutput(0.0);

        assertEquals("First output should be 1.234", 1.234, getScalarOutputForBlock(block, 0), DELTA);
        assertEquals("Second output should be 5.678", 5.678, getScalarOutputForBlock(block, 1), DELTA);
    }

    @Test
    public void testOutputDataTypes() {
        // Verify output data types are REAL (scalar)
        Matrix input = new Matrix(2, 1);
        input.set(0, 0, 5.0);
        input.set(1, 0, 3.0);

        setMatrixInput(0, input);
        block.calculateOutput(0.0);

        Data outputData1 = getOutputDataForBlock(block, 0);
        Data outputData2 = getOutputDataForBlock(block, 1);

        // Demux outputs are scalars (1x1 matrices are converted to REAL)
        assertTrue("Output 1 should be scalar or 1x1 matrix",
            outputData1.getDataType() == DataType.REAL ||
            (outputData1.getDataType() == DataType.MATRIX && outputData1.getMatrix().getRowDimension() == 1 && outputData1.getMatrix().getColumnDimension() == 1));
        assertTrue("Output 2 should be scalar or 1x1 matrix",
            outputData2.getDataType() == DataType.REAL ||
            (outputData2.getDataType() == DataType.MATRIX && outputData2.getMatrix().getRowDimension() == 1 && outputData2.getMatrix().getColumnDimension() == 1));
    }

    @Test
    public void testSequentialValues() {
        // Test demux with sequential input values
        Matrix input = new Matrix(2, 1);
        input.set(0, 0, 1.0);
        input.set(1, 0, 2.0);

        setMatrixInput(0, input);
        block.calculateOutput(0.0);

        assertEquals("First output should be 1.0", 1.0, getScalarOutputForBlock(block, 0), DELTA);
        assertEquals("Second output should be 2.0", 2.0, getScalarOutputForBlock(block, 1), DELTA);
    }

    @Test
    public void testIdenticalValues() {
        // Test demux with identical input values
        Matrix input = new Matrix(2, 1);
        input.set(0, 0, 7.5);
        input.set(1, 0, 7.5);

        setMatrixInput(0, input);
        block.calculateOutput(0.0);

        assertEquals("First output should be 7.5", 7.5, getScalarOutputForBlock(block, 0), DELTA);
        assertEquals("Second output should be 7.5", 7.5, getScalarOutputForBlock(block, 1), DELTA);
    }
}
